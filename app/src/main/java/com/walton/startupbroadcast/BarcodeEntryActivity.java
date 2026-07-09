package com.walton.startupbroadcast;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.walton.startupbroadcast.helper.ActivationDataService;
import com.walton.startupbroadcast.helper.BarcodeProcessor;
import com.walton.startupbroadcast.interfaces.ServerResponseSaveData;
import com.walton.startupbroadcast.pin.PinService;
import com.walton.startupbroadcast.repository.autoregistration.ImplAutoRegistration;
import com.walton.startupbroadcast.repository.display.ImplIDisplayRepository;
import com.walton.startupbroadcast.repository.emi.ImplEMIRepository;
import com.walton.startupbroadcast.retrofit.ResponseData;
import com.walton.startupbroadcast.retrofit.SubmitDataToServer;
import com.walton.startupbroadcast.utilities.UtilityClass;

public class BarcodeEntryActivity extends Activity implements TextWatcher {

    private EditText etBarcode;
    private LinearLayout lnProgress;
    private boolean isBarcodeScanned = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private ImplEMIRepository implEMIRepository;
    private ImplAutoRegistration implAutoRegistration;
    private ImplIDisplayRepository implIDisplayRepository;
    private UtilityClass utilityClass;
    private PinService pinService;
    private BarcodeProcessor barcodeProcessor;
    private ActivationDataService activationDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_entry);

        etBarcode = findViewById(R.id.inputBarcode);
        lnProgress = findViewById(R.id.lnProgress);
        etBarcode.addTextChangedListener(this);
        utilityClass = UtilityClass.getInstance(this);
        utilityClass.hideKeyPad(this);

        implAutoRegistration = ImplAutoRegistration.getInstance(getApplicationContext());
        implEMIRepository = ImplEMIRepository.getInstance(getApplicationContext());
        implIDisplayRepository = ImplIDisplayRepository.getInstance(getApplicationContext());

        pinService = new PinService();
        barcodeProcessor = new BarcodeProcessor(pinService, implAutoRegistration);
        activationDataService = new ActivationDataService(implAutoRegistration, implEMIRepository, implIDisplayRepository, pinService);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            etBarcode.setShowSoftInputOnFocus(false);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        handler.removeCallbacksAndMessages(null); // remove old callbacks

        handler.postDelayed(() -> {
            String barcode = editable.toString().trim().toUpperCase();
            if (isBarcodeValid(barcode) && !isBarcodeScanned) {
                handleBarcodeInput(barcode);
            }
        }, 300);
    }

    private boolean isBarcodeValid(String barcode) {
        return barcode.length() == 14 && (barcode.startsWith("W") || barcode.startsWith("M") || barcode.startsWith("L"));
    }

    private void handleBarcodeInput(String barcode) {
        if (!utilityClass.isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
            etBarcode.requestFocus();
            return;
        }
        isBarcodeScanned = true;
        resetBarcodeInput();

        BarcodeProcessor.BarcodeResult result = barcodeProcessor.processBarcode(barcode, implAutoRegistration.getMacAddress());
        saveDataToServer(result, barcode);
    }

    private void resetBarcodeInput() {
        etBarcode.clearFocus();
        etBarcode.setText("");
    }

    private void saveDataToServer(BarcodeProcessor.BarcodeResult result, String barcode) {
        utilityClass.hideKeyPad(this);
        etBarcode.setActivated(false);
        lnProgress.setVisibility(View.VISIBLE);

        SubmitDataToServer.getInstance().SaveData(
                result.emiPinString, result.passCodeString, result.displayPinString,
                result.displayPassCodesString,
                barcode, implAutoRegistration.getMacAddress(),
                new ServerResponseSaveData() {
                    @Override
                    public void Success(String response, String limitCounter) {
                        lnProgress.setVisibility(View.GONE);
                        // Save data to device
                        activationDataService.saveActivationData(result.passCodeList, result.emiPinList, result.displayPinList, result.displayPassCodes, barcode,
                                limitCounter, false, null, response);
                        Toast.makeText(BarcodeEntryActivity.this, "Data Saved Successfully "+implEMIRepository.getTvBarcode(), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void Error(String errorMessage) {
                        lnProgress.setVisibility(View.GONE);
                        resetBarcodeInput();
                        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void SyncData(ResponseData responseData, String limitCounter) {
                        // Save data to device
                        activationDataService.saveActivationData(result.passCodeList, result.emiPinList, result.displayPinList, result.displayPassCodes, barcode,
                                limitCounter, true, responseData, null);
                        Toast.makeText(getApplicationContext(), "Data Synchronization Complete!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isBarcodeScanned = false;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
}
