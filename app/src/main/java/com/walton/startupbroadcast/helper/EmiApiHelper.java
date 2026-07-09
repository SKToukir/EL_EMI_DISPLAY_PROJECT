package com.walton.startupbroadcast.helper;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.walton.startupbroadcast.interfaces.IPaymentStatus;
import com.walton.startupbroadcast.interfaces.IWarningMessage;
import com.walton.startupbroadcast.interfaces.ServerResponseCheckEMI;
import com.walton.startupbroadcast.model.WarningMessage;
import com.walton.startupbroadcast.repository.autoregistration.ImplAutoRegistration;
import com.walton.startupbroadcast.repository.emi.ImplEMIRepository;
import com.walton.startupbroadcast.retrofit.CallCleanStatus;
import com.walton.startupbroadcast.retrofit.CallEMIResponse;
import com.walton.startupbroadcast.retrofit.CheckPaymentStatus;

/**
 * Encapsulates all API calls related to EMI enforcement:
 * - EMI lock/unlock status check
 * - Payment method confirmation
 * - Device clean (factory reset) status
 */
public class EmiApiHelper {

    public interface EmiStatusCallback {
        /**
         * Server says device should be locked (status == 1).
         */
        void onShouldLock();

        /**
         * Server says device should be unlocked.
         */
        void onShouldUnlock();

        /**
         * API call failed.
         */
        void onError(String message);

        /**
         * Server returned updated counter values.
         */
        void onCounterUpdated(String currentCounter, String limitCounter);
    }

    public interface PaymentStatusCallback {
        void onEmi();

        void onComplete();

        void onNotSold();

        void onError(String message);
    }

    public interface IWarningCallBack {
        void onShow(String title, String messageBody);

        void onNotShow();
    }

    public interface CleanStatusCallback {
        void onCleanRequired();

        void onError(String message);
    }

    private static final String TAG = "EmiApiHelper";

    private final Context context;
    private final ImplAutoRegistration implAutoRegistration;
    private final ImplEMIRepository implEMIRepository;

    public EmiApiHelper(Context context,
                        ImplAutoRegistration implAutoRegistration,
                        ImplEMIRepository implEMIRepository) {
        this.context = context;
        this.implAutoRegistration = implAutoRegistration;
        this.implEMIRepository = implEMIRepository;
    }

    /**
     * Checks the EMI lock status from the server.
     * Also updates local counter state via {@link EmiStatusCallback#onCounterUpdated}.
     */
    public void checkEMIStatus(EmiStatusCallback callback) {
        Log.d(TAG, "checkEMIStatus: called");
        CallEMIResponse.getInstance().emiStatus(
                implAutoRegistration.getMacAddress(),
                implEMIRepository.getTvBarcode(),
                String.valueOf(implEMIRepository.getStartCounter()),
                new ServerResponseCheckEMI() {
                    @Override
                    public void Success(String response) {
                        if (Integer.parseInt(response) == 1) {
                            implEMIRepository.setShouldShowEMIDialog(true);
                            callback.onShouldLock();
                        } else {
                            implEMIRepository.setShouldShowEMIDialog(false);
                            callback.onShouldUnlock();
                        }
                    }

                    @Override
                    public void Error(String errorMessage) {
                        callback.onError(errorMessage);
                    }

                    @Override
                    public void SetCounter(String current_counter, String limit_counter) {
                        implEMIRepository.setCounterReset(Integer.parseInt(limit_counter));
                        implEMIRepository.setStartCounter(Integer.parseInt(current_counter));
                        callback.onCounterUpdated(current_counter, limit_counter);
                    }
                });
    }

    public void checkWarningMessage(IWarningCallBack iWarningCallBack) {
        CallEMIResponse.getInstance().getWarningMessage(implAutoRegistration.getMacAddress(), implEMIRepository.getTvBarcode(), new IWarningMessage() {
            @Override
            public void onSuccess(WarningMessage response) {
                if (response.isWarningStatus()){
                    iWarningCallBack.onShow(response.getReference_label(), response.getMessage());
                }else {
                    iWarningCallBack.onNotShow();
                }
            }

            @Override
            public void onError(String error) {
                iWarningCallBack.onNotShow();
            }
        });
    }

    /**
     * Schedules a re-check of EMI status after 60 seconds (used for auto-unlock polling).
     */
    public void scheduleAutoUnlockCheck(Runnable onCheck) {
        new Handler(Looper.getMainLooper()).postDelayed(onCheck, 60_000);
    }

    /**
     * Calls the payment status API to resolve ambiguous "cash" payment method state.
     * Returns one of: on_emi / complete / not_sold.
     */
    public void checkPaymentStatus(PaymentStatusCallback callback) {
        CheckPaymentStatus.getInstance().checkPaymentStatus(
                implEMIRepository.getTvBarcode(),
                new IPaymentStatus() {
                    @Override
                    public void SellStatus(String status) {
                        switch (status.toLowerCase()) {
                            case "on_emi":
                                callback.onEmi();
                                break;
                            case "complete":
                                callback.onComplete();
                                break;
                            case "not_sold":
                                callback.onNotSold();
                                break;
                        }
                    }

                    @Override
                    public void ResultFound(boolean isResultFound) {
                    }

                    @Override
                    public void Error(String message) {
                        callback.onError(message);
                    }
                });
    }

    /**
     * Checks if the server has flagged this device for a clean reset.
     * If yes, triggers {@link CleanStatusCallback#onCleanRequired()}.
     */
    public void checkCleanStatus(CleanStatusCallback callback) {
        CallCleanStatus.getInstance().CleanStatusCheck(
                implAutoRegistration.getMacAddress(),
                new ServerResponseCheckEMI() {
                    @Override
                    public void Success(String response) {
                        if ("1".equalsIgnoreCase(response)) {
                            callback.onCleanRequired();
                        }
                    }

                    @Override
                    public void Error(String errorMessage) {
                        callback.onError(errorMessage);
                    }

                    @Override
                    public void SetCounter(String current_counter, String limit_counter) {
                    }
                });
    }

    public boolean is90PercentReached() {

        int y = implEMIRepository.getStartCounter();
        int x = Integer.parseInt(implEMIRepository.getCounterReset());

        return x > 0 && (y * 100 >= 90 * x);
    }

    public boolean is95PercentReached() {

        int y = implEMIRepository.getStartCounter();
        int x = Integer.parseInt(implEMIRepository.getCounterReset());

        return x > 0 && (y * 100 >= 95 * x);
    }

    /**
     * Acknowledges the clean status back to server (resets flag to "0"),
     * then invokes {@code onCleaned} on success.
     */
    public void acknowledgeCleanStatus(Runnable onCleaned) {
        String macAddress = implAutoRegistration.getMacAddress();
        CallCleanStatus.getInstance().UpdateCleanStatus(macAddress, "0",
                new ServerResponseCheckEMI() {
                    @Override
                    public void Success(String response) {
                        onCleaned.run();
                    }

                    @Override
                    public void Error(String errorMessage) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void SetCounter(String current_counter, String limit_counter) {
                    }
                });
    }
}
