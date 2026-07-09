package com.walton.startupbroadcast.helper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.walton.startupbroadcast.R;
import com.walton.startupbroadcast.interfaces.ISellerInfo;
import com.walton.startupbroadcast.retrofit.CallEMIResponse;

/**
 * Manages the overlay lock window shown during EMI enforcement.
 * Handles WindowManager setup, keyboard wiring, and seller info display.
 */
public class LockWindowManager {

    public interface LockWindowCallback {
        void onSubmitClicked();
        void onPinTextChanged(String text);
    }

    private final Activity activity;
    private final LockWindowCallback callback;

    private WindowManager winManager;
    private View mainView;

    private ImageView imgSuccess;
    private RelativeLayout rlLockLayout;
    private RelativeLayout rlKeyboardLayout;
    private EditText etPinTextView;

    public LockWindowManager(Activity activity, LockWindowCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    /**
     * Inflates and attaches the lock overlay window.
     *
     * @param barcode    device barcode to display
     * @param passCode   current EMI pass code to display
     * @param macAddress device MAC address for seller info lookup
     */
    public void show(String barcode, String passCode, String macAddress) {
        winManager = activity.getWindowManager();
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainView = LayoutInflater.from(activity).inflate(R.layout.registration_layout, null);

        bindViews(barcode, passCode);
        setupKeyboard();
        fetchAndShowSellerInfo(macAddress, barcode);
        attachToWindow();
    }

    private void bindViews(String barcode, String passCode) {
        imgSuccess = mainView.findViewById(R.id.imgSuccess);

        TextView txtBarcode = mainView.findViewById(R.id.txtBarcode);
        txtBarcode.setText(barcode);

        TextView txtPassCode = mainView.findViewById(R.id.txtPassCode);
        txtPassCode.setText(passCode);

        rlLockLayout = mainView.findViewById(R.id.rlLockLayout);
        rlKeyboardLayout = mainView.findViewById(R.id.rlKeyboardLayout);

        Button btnSubmitEMICode = mainView.findViewById(R.id.btnSubmitEMICode);
        Button btnExit = mainView.findViewById(R.id.btnExit);

        btnSubmitEMICode.setOnClickListener(v -> {
            rlLockLayout.setVisibility(GONE);
            rlKeyboardLayout.setVisibility(VISIBLE);
            callback.onSubmitClicked();
        });

//        btnExit.setOnClickListener(v -> {
//
//        });
    }

    private void setupKeyboard() {
        etPinTextView = mainView.findViewById(R.id.inputBarcode);
        etPinTextView.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable editable) {
                callback.onPinTextChanged(editable.toString());
            }
        });

        ViewGroup keyboardLayout = mainView.findViewById(R.id.keyboardLayout);
        for (int i = 0; i < keyboardLayout.getChildCount(); i++) {
            View row = keyboardLayout.getChildAt(i);
            if (row instanceof LinearLayout) {
                for (int j = 0; j < ((LinearLayout) row).getChildCount(); j++) {
                    View key = ((LinearLayout) row).getChildAt(j);
                    if (key instanceof Button) {
                        ((Button) key).setOnClickListener(v -> {
                            String text = ((Button) v).getText().toString();
                            if ("⌫".equals(text)) {
                                String current = etPinTextView.getText().toString();
                                if (!current.isEmpty()) {
                                    etPinTextView.setText(current.substring(0, current.length() - 1));
                                }
                            }else if ("Back".equals(text)){
                                rlLockLayout.setVisibility(VISIBLE);
                                rlKeyboardLayout.setVisibility(GONE);
                            }
                            else {
                                etPinTextView.append(text);
                            }
                        });
                    }
                }
            }
        }
    }

    private void fetchAndShowSellerInfo(String macAddress, String barcode) {
        CallEMIResponse.getInstance().sellerInfo(macAddress, barcode, new ISellerInfo() {
            @Override
            public void Success(boolean status, String sellsPointName, String contact) {
                if (status) {
                    LinearLayout lnDealerInfo = mainView.findViewById(R.id.lnDealerInfo);
                    TextView contactView = mainView.findViewById(R.id.tv_contact_details);
                    TextView telephone = mainView.findViewById(R.id.tv_phone_number);
                    lnDealerInfo.setVisibility(VISIBLE);
                    contactView.setText(sellsPointName);
                    telephone.setText(contact);
                }
            }

            @Override
            public void Error(String error) {
                LinearLayout lnDealerInfo = mainView.findViewById(R.id.lnDealerInfo);
                if (lnDealerInfo != null) lnDealerInfo.setVisibility(GONE);
            }
        });
    }

    private void attachToWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.OPAQUE;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // No FLAG_NOT_FOCUSABLE — this window needs to receive touch AND keyboard input

        if (mainView.getWindowToken() == null) {
            winManager.addView(mainView, params);
        }
    }

    /**
     * Shows a success animation and removes the overlay after a delay.
     */
    public void showSuccessAndDismiss() {
        imgSuccess.setVisibility(VISIBLE);
        rlKeyboardLayout.setVisibility(GONE);
        rlLockLayout.setVisibility(GONE);
        new Handler().postDelayed(this::dismiss, 4000);
        activity.finishAffinity();
    }

    /**
     * Immediately removes the overlay window.
     */
    public void dismiss() {
        if (winManager != null && mainView != null) {
            winManager.removeView(mainView);
        }
    }

    /**
     * Resets the PIN input field.
     */
    public void resetPinInput() {
        if (etPinTextView != null) {
            etPinTextView.setText("");
            etPinTextView.setHint("Pin Code");
        }
    }

    /**
     * @return true if the overlay window has been created
     */
    public boolean isShowing() {
        return mainView != null;
    }

    public View getMainView() {
        return mainView;
    }
}
