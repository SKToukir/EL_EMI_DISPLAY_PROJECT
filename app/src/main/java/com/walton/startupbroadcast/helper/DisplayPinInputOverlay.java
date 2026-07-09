package com.walton.startupbroadcast.helper;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.walton.startupbroadcast.R;
import com.walton.startupbroadcast.utilities.FontManager;

public class DisplayPinInputOverlay implements View.OnFocusChangeListener, View.OnClickListener {

    public interface OnClickCallBack {
        void OnConfirmClickListener(String pinCode);
        void OnCancelClickListener();
    }

    private final Activity mActivity;
    private final WindowManager winManager;
    private final String barcode, passCode;
    private final OnClickCallBack onClickCallBack;

    private View mainView;
    private Button btnConfirm, btnExit;
    private EditText etPinInput;
    private TextView txtBarcode, txtPasscode, txtErrorMessage;

    public DisplayPinInputOverlay(Activity activity, String barcode, String passCode, OnClickCallBack onClickCallBack) {
        this.mActivity = activity;
        this.winManager = activity.getWindowManager();
        this.barcode = barcode;
        this.passCode = passCode;
        this.onClickCallBack = onClickCallBack;
    }

    public void show() {
        if (mainView != null) {
            // already showing
            return;
        }

        mainView = LayoutInflater.from(mActivity).inflate(R.layout.custom_alert_layout, null);
        initUI();
        attachToWindow();
    }

    private void initUI() {
        btnConfirm = mainView.findViewById(R.id.btnConfirm);
        btnExit = mainView.findViewById(R.id.btnCancel);
        etPinInput = mainView.findViewById(R.id.et_enter_pin);
        txtPasscode = mainView.findViewById(R.id.txtPasscode);
        txtBarcode = mainView.findViewById(R.id.txtBarcode);
        txtErrorMessage = mainView.findViewById(R.id.txtErrorMessage);

        txtBarcode.setText("Barcode: " + barcode);
        txtPasscode.setText("Passcode: " + passCode);

        FontManager.applyFont(btnExit, mActivity, "bangla_button_font.ttf");
        FontManager.applyFont(btnConfirm, mActivity, "bangla_button_font.ttf");

        btnConfirm.setOnClickListener(this);
        btnExit.setOnClickListener(this);

        btnExit.setOnFocusChangeListener(this);
        btnConfirm.setOnFocusChangeListener(this);

        btnExit.setDefaultFocusHighlightEnabled(true);

        // Intercept BACK so it dismisses this overlay instead of falling through
        // to whatever is beneath it in the WindowManager stack.
        mainView.setFocusableInTouchMode(true);
        mainView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (onClickCallBack != null) {
                    onClickCallBack.OnCancelClickListener();
                }
                return true;
            }
            return false;
        });
    }

    private int dpToPx(int dp) {
        float density = mActivity.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void attachToWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params.gravity = Gravity.CENTER;
        params.width = dpToPx(700);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // No FLAG_NOT_FOCUSABLE — this overlay needs D-pad focus for PIN entry
        // and must sit above DisplayWindowManager's mainView, so add it after.

        winManager.addView(mainView, params);

        mainView.post(() -> etPinInput.requestFocus());
    }

    public void dismiss() {
        if (mainView != null && mainView.getWindowToken() != null) {
            winManager.removeView(mainView);
        }
        mainView = null;
    }

    public boolean isShowing() {
        return mainView != null;
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()) {
            case R.id.btnCancel:
                btnExit.setBackgroundResource(b ? R.drawable.selected_background : R.drawable.unselected_button);
                break;
            case R.id.btnConfirm:
                btnConfirm.setBackgroundResource(b ? R.drawable.selected_background : R.drawable.unselected_button);
                break;
        }
    }

    public void showError(boolean isShow, String message){
        if (isShow){
            txtErrorMessage.setText(message);
        }else {
            txtErrorMessage.setText("");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCancel:
                if (onClickCallBack != null) {
                    onClickCallBack.OnCancelClickListener();
                }
                break;
            case R.id.btnConfirm:
                if (onClickCallBack != null) {
                    String pin = etPinInput.getText().toString().trim();
                    if (pin.isEmpty()) {
                        etPinInput.setError("Please input PIN");
                        return;
                    }
                    onClickCallBack.OnConfirmClickListener(pin);
                }
                break;
        }
    }
}