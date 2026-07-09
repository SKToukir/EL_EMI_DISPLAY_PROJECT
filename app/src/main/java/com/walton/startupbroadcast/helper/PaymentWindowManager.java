package com.walton.startupbroadcast.helper;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.walton.startupbroadcast.R;

public class PaymentWindowManager {

    public interface PaymentWindowManagerCallBack {
        /**
         * Called once the user has made a valid payment selection and pressed Complete Process.
         *
         * @param isEmi          true if EMI selected, false if Cash
         * @param installmentMonths 3 / 6 / 9 / 12 if EMI, 0 if Cash
         */
        void onPaymentSelectionComplete(boolean isEmi, int installmentMonths);
    }

    private Activity mActivity;
    private PaymentWindowManagerCallBack mCallback;

    private WindowManager winManager;
    private View mainView;

    private CheckBox checkBoxEMI, checkBoxCash;

    private Button btnCompleteProcess;

    private boolean isEmiSelected = false;
    private int selectedMonths = 0;

    public PaymentWindowManager(Activity activity, PaymentWindowManagerCallBack callBack) {
        this.mActivity = activity;
        this.mCallback = callBack;
    }

    public void showPaymentManager() {
        winManager = mActivity.getWindowManager();
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainView = LayoutInflater.from(mActivity).inflate(R.layout.payment_window_manager, null);

        bindViews();
        attachToWindow();
    }

    private void bindViews() {
        checkBoxEMI = mainView.findViewById(R.id.checkBoxEMI);
        checkBoxCash = mainView.findViewById(R.id.checkBoxCash);

        btnCompleteProcess = mainView.findViewById(R.id.btnCompleteProcess);

        // EMI vs Cash are mutually exclusive
        checkBoxEMI.setOnClickListener(v -> {
            if (checkBoxEMI.isChecked()) {
                checkBoxCash.setChecked(false);
                isEmiSelected = true;
                selectedMonths = 0;
                btnCompleteProcess.setVisibility(View.GONE); // need a month selected first
            } else {
                isEmiSelected = false;
                btnCompleteProcess.setVisibility(View.GONE);
            }
        });

        checkBoxCash.setOnClickListener(v -> {
            if (checkBoxCash.isChecked()) {
                checkBoxEMI.setChecked(false);
                isEmiSelected = false;
                selectedMonths = 0;
                btnCompleteProcess.setVisibility(View.VISIBLE);
            } else {
                btnCompleteProcess.setVisibility(View.GONE);
            }
        });


        btnCompleteProcess.setOnClickListener(v -> {
            if (mCallback != null) {
                mCallback.onPaymentSelectionComplete(isEmiSelected, selectedMonths);
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
        // No FLAG_NOT_FOCUSABLE — needed for D-pad navigation between checkboxes/button

        if (mainView.getWindowToken() == null) {
            winManager.addView(mainView, params);

            // Force initial D-pad focus onto the first interactive element
            mainView.post(() -> {
                View firstFocusable = mainView.findViewById(R.id.checkBoxEMI);
                if (firstFocusable != null) {
                    firstFocusable.requestFocus();
                }
            });
        }
    }

    /**
     * Immediately removes the payment overlay window.
     */
    public void dismiss() {
        if (winManager != null && mainView != null) {
            winManager.removeView(mainView);
        }
    }

    public boolean isShowing() {
        return mainView != null;
    }

    public View getMainView() {
        return mainView;
    }
}