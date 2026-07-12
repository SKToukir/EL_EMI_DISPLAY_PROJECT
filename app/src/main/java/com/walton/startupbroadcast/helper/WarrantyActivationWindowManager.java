package com.walton.startupbroadcast.helper;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.walton.startupbroadcast.R;
import com.walton.startupbroadcast.model.ActivationModel;
import com.walton.startupbroadcast.pin.PinService;
import com.walton.startupbroadcast.repository.autoregistration.ImplAutoRegistration;
import com.walton.startupbroadcast.repository.display.ImplIDisplayRepository;
import com.walton.startupbroadcast.repository.emi.ImplEMIRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class WarrantyActivationWindowManager {

    private static final String TAG = "WarrantyActivationWM";

    public interface WarrantyActivationCallback {
        void onActivationConfirmed(String activationCode);

        void onActivateLater();
    }

    private final Activity mActivity;
    private final WarrantyActivationCallback mCallback;

    private WindowManager winManager;
    private View mainView;

    private EditText etActivationCode;
    private TextView txtCodeError, txtTvBarcode;
    private Button btnActivateWarranty;
    private Button btnActivateLater;
    private LinearLayout llBenefits;

    private ImplEMIRepository implEMIRepository;
    private ImplIDisplayRepository implIDisplayRepository;
    private ImplAutoRegistration implAutoRegistration;

    private boolean pinCodeMatched;

    private static class Benefit {
        final int iconRes;
        final String label;

        Benefit(int iconRes, String label) {
            this.iconRes = iconRes;
            this.label = label;
        }
    }

    private final Benefit[] benefits = new Benefit[]{
            new Benefit(R.drawable.ic_verified_user, "Official Warranty Protection"),
            new Benefit(R.drawable.ic_bolt, "Faster Service Support"),
            new Benefit(R.drawable.ic_qr_code, "Easy Product Verification"),
            new Benefit(R.drawable.ic_lock, "Secure Ownership Registration"),
    };

    public WarrantyActivationWindowManager(Activity activity, WarrantyActivationCallback callback) {
        this.mActivity = activity;
        this.mCallback = callback;
    }

    public void show() {
        winManager = mActivity.getWindowManager();
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainView = LayoutInflater.from(mActivity).inflate(R.layout.activity_warranty_activation, null);

        implEMIRepository = new ImplEMIRepository(mActivity);
        implIDisplayRepository = new ImplIDisplayRepository(mActivity);
        implAutoRegistration = new ImplAutoRegistration(mActivity);

        bindViews();
        populateBenefits();
        setupCodeInput();
        setupButtons();
        attachToWindow();
        setTvBarcode(implEMIRepository.getTvBarcode());
    }

    private void bindViews() {
        txtTvBarcode = mainView.findViewById(R.id.txtTvBarcode);
        etActivationCode = mainView.findViewById(R.id.etActivationCode);
        txtCodeError = mainView.findViewById(R.id.txtCodeError);
        btnActivateWarranty = mainView.findViewById(R.id.btnActivateWarranty);
        btnActivateLater = mainView.findViewById(R.id.btnActivateLater);
        llBenefits = mainView.findViewById(R.id.llBenefits);
    }

    private void populateBenefits() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        for (int i = 0; i < benefits.length; i++) {
            Benefit b = benefits[i];
            View row = inflater.inflate(R.layout.item_benefit_card, llBenefits, false);

            ImageView icon = row.findViewById(R.id.ivBenefitIcon);
            TextView label = row.findViewById(R.id.txtBenefitLabel);

            icon.setImageResource(b.iconRes);
            icon.setColorFilter(
                    ContextCompat.getColor(mActivity, R.color.warranty_success),
                    PorterDuff.Mode.SRC_IN
            );
            label.setText(b.label);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) row.getLayoutParams();
            if (i > 0) {
                row.setLayoutParams(params);
            }
            llBenefits.addView(row);
        }
    }

    private void setupCodeInput() {
        etActivationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtCodeError.getVisibility() == View.VISIBLE) {
                    txtCodeError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupButtons() {
        btnActivateWarranty.setOnClickListener(v -> attemptActivation());

        btnActivateLater.setOnClickListener(v -> {
            dismiss();
            if (mCallback != null) {
                mCallback.onActivateLater();
            }
        });
    }

    // New public method to set the barcode once you have it (e.g. from device serial / prefs)
    public void setTvBarcode(String barcode) {
        if (txtTvBarcode != null && barcode != null) {
            txtTvBarcode.setText(barcode);
        }
    }

    private void attemptActivation() {
        String code = etActivationCode.getText().toString().trim().toUpperCase();

        if (code.isEmpty()) {
            showError("Please enter your activation code");
            return;
        }
        if (code.length() < 8) {
            showError("Activation code looks too short — please check and try again");
            return;
        }
        if (!activationCodeNotMatched(code)) {
            showError("Enter correct Activation Code");
            return;
        }

        // Replace with your real e-warranty verification call
        // e.g. call waltontvrni.com verification endpoint here
        dismiss();
        if (mCallback != null) {
            mCallback.onActivationConfirmed(code);
        }
    }

    private boolean activationCodeNotMatched(String code) {
        return checkPinCode(generateSecondHashPinCode(code.toLowerCase()));
    }

    private void showError(String message) {
        txtCodeError.setText(message);
        txtCodeError.setVisibility(View.VISIBLE);
    }

    private void attachToWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        params.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.OPAQUE;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // No FLAG_NOT_FOCUSABLE — this screen needs D-pad + EditText focus

        if (mainView.getWindowToken() == null) {
            try {
                winManager.addView(mainView, params);

                // Activation code field receives focus first, per UX requirement
                mainView.post(() -> {
                    if (etActivationCode != null) {
                        etActivationCode.requestFocus();
                    }
                });
            } catch (WindowManager.BadTokenException e) {
                Log.e(TAG, "BadTokenException adding warranty activation overlay", e);
            }
        }
    }

    /**
     * Immediately removes the warranty activation overlay window.
     */
    public void dismiss() {
        if (winManager != null && mainView != null) {
            try {
                winManager.removeView(mainView);
            } catch (Exception e) {
                Log.e(TAG, "Error removing warranty activation overlay", e);
            } finally {
                mainView = null;
            }
        }
    }

    public boolean isShowing() {
        return mainView != null;
    }

    public View getMainView() {
        return mainView;
    }

    private int dpToPx(int dp) {
        float density = mActivity.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /*
     * Updated V3 Added below method
     * */
    private String getPinCode(String code) {
        return code.split("\\s+")[0];
    }

    private String generateSecondHashPinCode(String s) {
        String combinedString = s + implAutoRegistration.getMacAddress() + s;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(combinedString.getBytes());

            // Convert hashed bytes to hexadecimal representation
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            // Take the first 8 characters and capitalize them
            return hexString.substring(0, 8).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean checkPinCode(String pinCode) {
        List<ActivationModel> activationModelList = implEMIRepository.getActivationList();
        if (getPinCode(activationModelList.get(0).getCode()).equalsIgnoreCase(pinCode)) {
            if (!activationModelList.get(0).isPaid()) {
                activationModelList.get(0).setPaid(true);
                Log.d(TAG, "checkPincode: " + getPinCode(activationModelList.get(0).getCode()) + "\n" + pinCode);
                implEMIRepository.saveActivationData(activationModelList);
                pinCodeMatched = true;
            } else {
                pinCodeMatched = true;
            }
        }
        return pinCodeMatched;
    }


}