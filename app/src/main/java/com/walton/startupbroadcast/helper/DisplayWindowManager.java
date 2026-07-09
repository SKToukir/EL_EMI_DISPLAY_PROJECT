package com.walton.startupbroadcast.helper;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.walton.startupbroadcast.R;
import com.walton.startupbroadcast.repository.display.ImplIDisplayRepository;

public class DisplayWindowManager {

    public interface DisplayWindowManagerCallBack {
        void selectSetPayment();

        void selectDisplay();
    }

    private int totalCount = 0;
    private Activity mActivity;
    private DisplayWindowManagerCallBack mCallback;

    private WindowManager winManager;
    private View mainView;

    private Button btnSetPayment, btnDisplay;
    private TextView txtRemainingDisplayCount;

    public DisplayWindowManager(Activity activity, DisplayWindowManagerCallBack callBack) {
        this.mCallback = callBack;
        this.mActivity = activity;
    }

    public void showDisplayManager(ImplIDisplayRepository implIDisplayRepository) {
        winManager = mActivity.getWindowManager();
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainView = LayoutInflater.from(mActivity).inflate(R.layout.display_window_manager, null);

        bindViews(implIDisplayRepository);
        attachToWindow();
    }

    private void bindViews(ImplIDisplayRepository implIDisplayRepository) {
        btnSetPayment = mainView.findViewById(R.id.btn_set_payment);
        btnDisplay = mainView.findViewById(R.id.btn_select_display);

        txtRemainingDisplayCount = mainView.findViewById(R.id.txtRemainingDisplayCount);


        updateCounter(implIDisplayRepository);

        btnSetPayment.setOnClickListener(v -> {
            mCallback.selectSetPayment();
        });

        btnDisplay.setOnClickListener(v -> {
            mCallback.selectDisplay();
        });
    }

    public void updateCounter(ImplIDisplayRepository implIDisplayRepository) {
        if (implIDisplayRepository.getDisplayProductionMode()) {
            totalCount = 10;
        } else {
            totalCount = 30 + 10; // here i added 10 because increment starts with 11
        }

        int remainingCounter = totalCount - Integer.parseInt(implIDisplayRepository.getTotalDisplayCounter());
        txtRemainingDisplayCount.setText("Remaining Display Boot Count: " + remainingCounter);
        btnDisplay.setText("Display Mode (" + remainingCounter + ")");

        // Change text color if below 10
        if (remainingCounter < 10) {
            txtRemainingDisplayCount.setTextColor(Color.RED);
        } else {
            txtRemainingDisplayCount.setTextColor(Color.WHITE); // default color
        }

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
        // No FLAG_NOT_FOCUSABLE — overlay must be focusable for D-pad navigation

        if (mainView.getWindowToken() == null) {
            winManager.addView(mainView, params);

            // Force initial focus onto the overlay's first interactive view
            mainView.post(() -> {
                View firstFocusable = mainView.findViewById(R.id.btn_set_payment);
                if (firstFocusable != null) {
                    firstFocusable.requestFocus();
                }
            });
        }
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
     * @return true if the overlay window has been created
     */
    public boolean isShowing() {
        return mainView != null;
    }

    public View getMainView() {
        return mainView;
    }
}
