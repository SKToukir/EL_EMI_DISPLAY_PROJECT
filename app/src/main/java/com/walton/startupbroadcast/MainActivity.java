package com.walton.startupbroadcast;

import static android.widget.Toast.LENGTH_LONG;
import static com.walton.startupbroadcast.utilities.Config.PAYMENT_METHOD_CASH;
import static com.walton.startupbroadcast.utilities.Config.PAYMENT_METHOD_INSTALLMENT;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import static com.walton.startupbroadcast.utilities.Config.TAG;

import com.walton.startupbroadcast.helper.DisplayCodeProcessor;
import com.walton.startupbroadcast.helper.DisplayPinInputOverlay;
import com.walton.startupbroadcast.helper.DisplayWindowManager;
import com.walton.startupbroadcast.helper.EmiApiHelper;
import com.walton.startupbroadcast.helper.LockWindowManager;
import com.walton.startupbroadcast.helper.MainUiController;
import com.walton.startupbroadcast.helper.PaymentWindowManager;
import com.walton.startupbroadcast.helper.PinVerificationHelper;
import com.walton.startupbroadcast.helper.WarrantyActivationWindowManager;
import com.walton.startupbroadcast.model.ActivationModel;
import com.walton.startupbroadcast.repository.autoregistration.ImplAutoRegistration;
import com.walton.startupbroadcast.repository.display.ImplIDisplayRepository;
import com.walton.startupbroadcast.repository.emi.ImplEMIRepository;
import com.walton.startupbroadcast.utilities.UtilityClass;

import java.util.List;

/**
 * MainActivity — entry point after boot.
 * <p>
 * Responsibilities (only):
 * 1. Decide the startup flow (barcode saved? registered? EMI status?)
 * 2. Coordinate helpers: UI, lock window, PIN verification, API calls
 * 3. Manage lifecycle and the Handler message loop
 * <p>
 * All other logic lives in dedicated helper classes.
 */
public class MainActivity extends Activity
        implements MainUiController.UiEventCallback,
        LockWindowManager.LockWindowCallback,
        PinVerificationHelper.PinVerificationCallback, DisplayWindowManager.DisplayWindowManagerCallBack, PaymentWindowManager.PaymentWindowManagerCallBack {

    // ─── Handler message IDs ───────────────────────────────────────────────────
    public static final int INTERNET_STATUS = 123;
    public static final int GET_MAC = 125;
    public static final int CHECK_REGISTRATION = 126;
    public static final int CHECK_EMI_STATUS = 128;

    // ─── EMI constants ─────────────────────────────────────────────────────────
    public static final int TOTAL_EMI_MONTHS = 36;
    public static final int EMI_BOOT_UP_COUNTER_LIMIT = 250;
    public static final int EMI_AP_OPEN_COUNTER_LIMIT = 1;

    public static boolean isActivityOpen = false;

    // ─── Helpers ───────────────────────────────────────────────────────────────
    private UtilityClass utilityClass;
    private ImplAutoRegistration implAutoRegistration;
    private ImplEMIRepository implEMIRepository;
    private ImplIDisplayRepository implIDisplayRepository;
    private MainUiController uiController;
    private LockWindowManager lockWindowManager;
    private DisplayWindowManager displayWindowManager;
    private WarrantyActivationWindowManager warrantyActivationWindowManager;
    private PinVerificationHelper pinHelper;
    private EmiApiHelper emiApiHelper;

    // ─── State ─────────────────────────────────────────────────────────────────
    static String deviceBarcode;
    static String finalMacAddress;

    // ─── Handler ───────────────────────────────────────────────────────────────
    private final Handler handler = new Handler(message -> {
        switch (message.what) {
            case INTERNET_STATUS:
                // Network state noted; no further action needed here
                return true;

            case GET_MAC:
                finalMacAddress = implAutoRegistration.getMacAddress();
                return true;

            case CHECK_REGISTRATION:
                handleCheckRegistration();
                return true;

            case CHECK_EMI_STATUS:
                if (utilityClass.isNetworkAvailable()) {
                    checkEMIStatusFromServer();
                }
                return true;
        }
        return false;
    });

    // ─── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDependencies();
        uiController.init();

        if (utilityClass.isNetworkAvailable() && isBarcodeSaved()) {
            checkCleanStatus();
        }

        decideStartupFlow();

        handler.sendEmptyMessage(GET_MAC);
        checkNetwork();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityOpen = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityOpen = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwork();
    }

    @Override
    public void onBackPressed() {
        if (lockWindowManager != null && lockWindowManager.isShowing()) {
            Toast.makeText(this, "Please Complete Registration Process", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    // ─── Initialisation ────────────────────────────────────────────────────────

    private void initDependencies() {
        utilityClass = UtilityClass.getInstance(this);
        implAutoRegistration = ImplAutoRegistration.getInstance(this);
        implEMIRepository = ImplEMIRepository.getInstance(this);
        implIDisplayRepository = ImplIDisplayRepository.getInstance(this);

        uiController = new MainUiController(this, this);
        lockWindowManager = new LockWindowManager(MainActivity.this, this);
        displayWindowManager = new DisplayWindowManager(MainActivity.this, this);
        emiApiHelper = new EmiApiHelper(this, implAutoRegistration, implEMIRepository);

        warrantyActivationWindowManager = new WarrantyActivationWindowManager(MainActivity.this, new WarrantyActivationWindowManager.WarrantyActivationCallback() {
            @Override
            public void onActivationConfirmed(String activationCode) {
                warrantyActivationWindowManager.dismiss();

                activateAndSetEMIData();
                Toast.makeText(getApplicationContext(), "Your device is successfully activated!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onActivateLater() {
                warrantyActivationWindowManager.dismiss();
                displayWindowManager.showDisplayManager(implIDisplayRepository);
            }
        });
        // pinHelper is created lazily when MAC address is available
    }

    // ─── Startup flow decision ─────────────────────────────────────────────────

    private void decideStartupFlow() {
        if (!isBarcodeSaved()) {
            openBarcodeEntryActivity();
            return;
        }

        if (implAutoRegistration.isRegistered()) {
            finish();
            return;
        }

        /*
         * Check Display status if true then show the
         * display lock screen for choosing the option
         * */
        if (implIDisplayRepository.getDisplayStatus()) {
            /*
             * TODO
             *  show Display layout as a lock screen
             *  Customer must select a option
             *  Cant skip this layout
             * */
            showDisplayScreen();
            return;
        }

        if (implEMIRepository.shouldShowEMIDialog()) {
            showLockScreen();
        } else {
            deviceBarcode = implEMIRepository.getTvBarcode();
            String paymentMethod = implEMIRepository.getPaymentMethod();
            boolean paymentMethodSet = paymentMethod.equalsIgnoreCase(PAYMENT_METHOD_CASH)
                    || paymentMethod.equalsIgnoreCase(PAYMENT_METHOD_INSTALLMENT);

            if (!paymentMethodSet) {
                uiController.showEMILayout();
                handler.sendEmptyMessage(CHECK_EMI_STATUS); // urgent lock check
            } else {
                implEMIRepository.setStartCounter(implEMIRepository.getStartCounter() + 1);
                handler.sendEmptyMessage(CHECK_REGISTRATION);
            }
        }
    }

    // ─── Handler: CHECK_REGISTRATION ──────────────────────────────────────────

    private void handleCheckRegistration() {
        if (!utilityClass.isNetworkAvailable()) {
            handleOfflineRegistrationCheck();
        } else {
            handleOnlineRegistrationCheck();
        }
    }

    private void handleOfflineRegistrationCheck() {
        boolean counterExceeded = implEMIRepository.getEMIStatus()
                && implEMIRepository.getStartCounter()
                > Integer.parseInt(implEMIRepository.getCounterReset());


        if (counterExceeded) {
            implEMIRepository.setShouldShowEMIDialog(true);
            showLockScreen();
        } else if (emiApiHelper.is90PercentReached() || emiApiHelper.is95PercentReached()) {
            uiController.showWarningMessageDialog(" ", getString(R.string.warning_message));
        } else {
            finish();
        }
    }

    private void handleOnlineRegistrationCheck() {
        if (implEMIRepository.getPaymentMethod().equalsIgnoreCase(PAYMENT_METHOD_CASH)) {
            checkPaymentStatusFromServer();
        } else if (implEMIRepository.getEMIStatus()) {
            checkEMIStatusFromServer();
        } else {
            finish();
        }
    }

    // ─── Lock screen ───────────────────────────────────────────────────────────

    private void showLockScreen() {
        pinHelper = new PinVerificationHelper(implAutoRegistration.getMacAddress());
        if (lockWindowManager != null && !lockWindowManager.isShowing()) {
            lockWindowManager.show(
                    implEMIRepository.getTvBarcode(),
                    pinHelper.getNextDisplayPassCode(implEMIRepository.getActivationList()),
                    implAutoRegistration.getMacAddress()
            );
            scheduleAutoUnlockCheck();
        }
    }
    // ─── LockWindowManager.LockWindowCallback ─────────────────────────────────


    @Override
    public void onSubmitClicked() {
        // Keyboard is now visible; nothing extra needed in activity
    }

    @Override
    public void onPinTextChanged(String text) {
        if (text.length() == 8) {
            if (pinHelper == null) {
                pinHelper = new PinVerificationHelper(implAutoRegistration.getMacAddress());
            }
            pinHelper.verify(text, implEMIRepository.getActivationList(), this);
        }
    }

    // ─── PinVerificationHelper.PinVerificationCallback ────────────────────────

    @Override
    public void onPinMatched(List<ActivationModel> updatedList) {
        implEMIRepository.saveActivationData(updatedList);
        implEMIRepository.setShouldShowEMIDialog(false);
        implEMIRepository.setStartCounter(EMI_AP_OPEN_COUNTER_LIMIT + 1);

        boolean anyDue = updatedList.stream().anyMatch(m -> !m.isPaid());
        if (!anyDue) {
            markDeviceFullyPaid();
        }

        lockWindowManager.showSuccessAndDismiss();
    }

    @Override
    public void onAllEmiComplete() {
        implEMIRepository.saveActivationData(implEMIRepository.getActivationList());
        markDeviceFullyPaid();
        implEMIRepository.setStartCounter(EMI_AP_OPEN_COUNTER_LIMIT + 1);
        lockWindowManager.showSuccessAndDismiss();
    }

    @Override
    public void onAlreadyPaid() {
        Toast.makeText(this, "This PIN Code Already Used", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPinNotMatched() {
        Toast.makeText(this, "Pin Code Not Matched", Toast.LENGTH_SHORT).show();
        lockWindowManager.resetPinInput();
    }

    private void markDeviceFullyPaid() {
        implAutoRegistration.setRegistrationStatus(true);
        implEMIRepository.setEMIStatus(false);
        implEMIRepository.setShouldShowEMIDialog(false);
        Toast.makeText(this, getString(R.string.emi_complete), Toast.LENGTH_SHORT).show();
    }

    // ─── MainUiController.UiEventCallback ─────────────────────────────────────

    @Override
    public void onCompleteProcessClicked() {
        if (uiController.isCashSelected()) {
            activateAndSetCash();
        } else if (uiController.isEmiSelected()) {
            activateAndSetEMIData();
        }
        new Handler().postDelayed(this::finish, 2000);
    }

    @Override
    public void onOpenNetworkSettings() {
        utilityClass.openNetworkSettings();
    }

    // ─── Payment method activation ─────────────────────────────────────────────

    private void activateAndSetCash() {
        implEMIRepository.setEMIStatus(false);
        implEMIRepository.setEMIDuration(String.valueOf(0));
        implAutoRegistration.setRegistrationStatus(false);
        implEMIRepository.savePaymentMethod(PAYMENT_METHOD_CASH);
        implEMIRepository.setShouldShowEMIDialog(false);
        Toast.makeText(this, "Congratulations! Your Device is now ready", Toast.LENGTH_SHORT).show();
    }

    private void activateAndSetEMIData() {
        implEMIRepository.setEMIDuration(String.valueOf(0));
        implEMIRepository.setEMIStatus(true);
        implEMIRepository.savePaymentMethod(PAYMENT_METHOD_INSTALLMENT);
        implEMIRepository.setShouldShowEMIDialog(false);
        implEMIRepository.setStartCounter(EMI_AP_OPEN_COUNTER_LIMIT);

        implIDisplayRepository.setDisplayStatus(false);
        implIDisplayRepository.setDisplayProductionMode(false);

        Toast.makeText(this, "EMI Activated", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void registeredDevice() {
        implEMIRepository.setEMIStatus(false);
        implEMIRepository.setEMIDuration(String.valueOf(0));
        implAutoRegistration.setRegistrationStatus(true);
        implEMIRepository.savePaymentMethod(PAYMENT_METHOD_CASH);
        implEMIRepository.setShouldShowEMIDialog(false);
        Toast.makeText(this, "Congratulations! Your Device is now ready", Toast.LENGTH_SHORT).show();
        finish();
    }

    // ─── API calls (delegated to EmiApiHelper) ─────────────────────────────────

    private void checkEMIStatusFromServer() {
        emiApiHelper.checkEMIStatus(new EmiApiHelper.EmiStatusCallback() {
            @Override
            public void onShouldLock() {
                showLockScreen();
            }

            @Override
            public void onShouldUnlock() {
                emiApiHelper.checkWarningMessage(new EmiApiHelper.IWarningCallBack() {
                    @Override
                    public void onShow(String title, String messageBody) {
                        uiController.showWarningMessageDialog(title, messageBody);
                    }

                    @Override
                    public void onNotShow() {
                        String method = implEMIRepository.getPaymentMethod();
                        if (method.equalsIgnoreCase(PAYMENT_METHOD_CASH)
                                || method.equalsIgnoreCase(PAYMENT_METHOD_INSTALLMENT)) {
                            lockWindowManager.dismiss();
                            finishAffinity();
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCounterUpdated(String current, String limit) {
                // Already persisted inside EmiApiHelper
            }
        });
    }

    private void checkPaymentStatusFromServer() {
        emiApiHelper.checkPaymentStatus(new EmiApiHelper.PaymentStatusCallback() {
            @Override
            public void onEmi() {
                activateAndSetEMIData();
            }

            @Override
            public void onComplete() {
                registeredDevice();
            }

            @Override
            public void onNotSold() {
                finish();
            }

            @Override
            public void onError(String message) {
                finish();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkCleanStatus() {
        emiApiHelper.checkCleanStatus(new EmiApiHelper.CleanStatusCallback() {
            @Override
            public void onCleanRequired() {
                emiApiHelper.acknowledgeCleanStatus(MainActivity.this::makeFresh);
            }

            @Override
            public void onError(String message) {
                finish();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleAutoUnlockCheck() {
        emiApiHelper.scheduleAutoUnlockCheck(() -> {
            if (utilityClass.isNetworkAvailable()) {
                checkEMIStatusFromServer();
            }
        });
    }

    // ─── Utilities ─────────────────────────────────────────────────────────────

    private boolean isBarcodeSaved() {
        String barcode = implEMIRepository.getTvBarcode();
        return (barcode.startsWith("M") || barcode.startsWith("W") || barcode.startsWith("L"))
                && barcode.length() == 14;
    }

    private void openBarcodeEntryActivity() {
        Intent intent = new Intent(this, BarcodeEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void checkNetwork() {
        handler.sendMessage(handler.obtainMessage(
                INTERNET_STATUS, utilityClass.isNetworkAvailable()));
    }

    private void makeFresh() {
        implAutoRegistration.setRegistrationStatus(false);
        implEMIRepository.setStartCounter(0);
        implEMIRepository.setEMIStatus(false);
        implEMIRepository.setShouldShowEMIDialog(false);
        implEMIRepository.saveBarcode("sdsdsd");
        implEMIRepository.savePaymentMethod("null");
        /*
         * Display Project
         * */
        implIDisplayRepository.setDisplayProductionMode(true);
        implIDisplayRepository.setDisplayStatus(true);
        implIDisplayRepository.setDisplayCount(0);
        displayWindowManager.dismiss();
        Toast.makeText(this, getString(R.string.device_clean), Toast.LENGTH_SHORT).show();
        finish();
    }

    // ------------- Show Display Screen ------------------- //

    private void showDisplayScreen() {
        if (displayWindowManager != null && !displayWindowManager.isShowing()) {
            displayWindowManager.showDisplayManager(implIDisplayRepository);
        }
    }

    // ─── DisplayWindowManager.DisplayManagerCallback ─────────────────────────────────

    @Override
    public void selectSetPayment() {
        Log.d(TAG, "selectSetPayment: 1");
        displayWindowManager.dismiss();
        warrantyActivationWindowManager.show();
    }

    @Override
    public void selectDisplay() {
        handleDisplayLogic();
    }


    private void handleDisplayLogic() {
        Log.d(TAG, "handleDisplayLogic: Production Status:" + implIDisplayRepository.getDisplayProductionMode());
        Log.d(TAG, "handleDisplayLogic: Display Count:" + implIDisplayRepository.getDisplayCount());
        Log.d(TAG, "handleDisplayLogic: Display Counter:" + implIDisplayRepository.getTotalDisplayCounter());

        if ((implIDisplayRepository.getDisplayProductionMode()
                && Integer.parseInt(implIDisplayRepository.getTotalDisplayCounter()) == 10)
                || !implIDisplayRepository.getDisplayProductionMode()
                && Integer.parseInt(implIDisplayRepository.getTotalDisplayCounter()) == 30) {
            Log.d(TAG, "handleDisplayLogic: Called");
            showRequestCodeAlert();
        } else {
            implIDisplayRepository.saveTotalDisplayCounter(Integer.parseInt(implIDisplayRepository.getTotalDisplayCounter()) + 1);
            Log.d(TAG, "handleDisplayLogics: " + implIDisplayRepository.getTotalDisplayCounter());
//            updateRemainingDisplayMessage();
            displayWindowManager.updateCounter(implIDisplayRepository);
            launchHomeDisplayMode();
        }
    }

    private DisplayPinInputOverlay pinOverlayRef;
    private void showRequestCodeAlert() {
        DisplayCodeProcessor displayCodeProcessor = new DisplayCodeProcessor(this);

        DisplayPinInputOverlay pinOverlay = new DisplayPinInputOverlay(
                this,
                implEMIRepository.getTvBarcode(),
                getNextPassDisplayCode(),
                new DisplayPinInputOverlay.OnClickCallBack() {
                    @Override
                    public void OnConfirmClickListener(String pinCode) {
                        Log.d(TAG, "OnConfirmClickListener: "+pinCode);
                        if (displayCodeProcessor.isCodeIsValid(pinCode)) {
                            implIDisplayRepository.setDisplayProductionMode(false);
                            implIDisplayRepository.saveTotalDisplayCounter(11);

                            pinOverlayRef.dismiss();
                            launchHomeDisplayMode();
                        } else {
                            pinOverlayRef.showError(true,"Try again! Code not valid!");
                        }
                    }

                    @Override
                    public void OnCancelClickListener() {
                        pinOverlayRef.dismiss();
                    }
                });

        pinOverlayRef = pinOverlay; // keep a field reference so callbacks can call dismiss()
        pinOverlayRef.show();
    }

    private String getNextPassDisplayCode() {
        List<ActivationModel> pinPassCodeList = implIDisplayRepository.getDisplayPinList();
        Log.d(TAG, "getNextPassDisplayCode: "+pinPassCodeList);
        String passCode = "";
        for (int i = 0; i < pinPassCodeList.size(); i++) {
            if (!pinPassCodeList.get(i).isPaid()) {
                passCode = getPassCode(pinPassCodeList.get(i).getCode());
                break;
            }
        }
        Log.d(TAG, "getNextPassDisplayCode: "+passCode);
        return passCode;
    }

    /*
     * Updated V3 Added below method
     * */
    private String getPassCode(String code) {
        return code.substring(code.indexOf(" ") + 1);
    }

    private void launchHomeDisplayMode() {
        Intent intent = new Intent(this, OverlayService.class);
        Log.d(TAG, "launchHomeDisplayMode: 1");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
            Log.d(TAG, "launchHomeDisplayMode: 2");
        } else {
            startService(intent);
            Log.d(TAG, "launchHomeDisplayMode: 3");
        }
        Log.d(TAG, "launchHomeDisplayMode: 4");
        displayWindowManager.dismiss();
        finish();
    }

    @Override
    public void onPaymentSelectionComplete(boolean isEmi, int installmentMonths) {

    }
}
