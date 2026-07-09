package com.walton.startupbroadcast.helper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.walton.startupbroadcast.utilities.Config.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.walton.startupbroadcast.OverlayService;
import com.walton.startupbroadcast.R;
import com.walton.startupbroadcast.dialog.CustomCautionDialog;
import com.walton.startupbroadcast.dialog.WarningMessageDialog;

/**
 * Owns all view references and wiring for the EMI selection screen in MainActivity.
 * Keeps the activity free from findViewById, checkbox logic, and focus handling.
 */
public class MainUiController implements View.OnFocusChangeListener,
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    public interface UiEventCallback {
        void onCompleteProcessClicked();

        void onOpenNetworkSettings();
    }

    private final Activity activity;
    private final UiEventCallback callback;

    // Views
    private CheckBox checkBoxEMI, checkBoxCash;
    private CheckBox checkBoxThree, checkBoxSix, checkBoxNine, checkBoxTwelve;
    private Button btnCompleteProcess;
    private TextView txtPressOkButton;

    public RelativeLayout rlMainLayout;
    public RelativeLayout rlEMILayout;
    public LinearLayout lnProgress;
    public LinearLayout lnDealerInfo;
    public TextView txtBarcode;
    public TextView txtPassCode;
    public TextView tvContactDetails;
    public TextView tvPhoneNumber;
    public EditText inputPincode;
    public Button btnSubmitEMICode;
    public ImageView imgSuccess;

    private CustomCautionDialog customCautionDialog;
    private WarningMessageDialog warningMessageDialog;

    public MainUiController(Activity activity, UiEventCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    /**
     * Finds and wires all views. Call from {@code Activity.onCreate()} after {@code setContentView}.
     */
    public void init() {
        txtPassCode = activity.findViewById(R.id.txtPassCode);
        inputPincode = activity.findViewById(R.id.inputPincode);
        rlEMILayout = activity.findViewById(R.id.rlEMILayout);
        txtBarcode = activity.findViewById(R.id.txtBarcode);
        lnDealerInfo = activity.findViewById(R.id.lnDealerInfo);
        btnSubmitEMICode = activity.findViewById(R.id.btnSubmitEMICode);
        tvContactDetails = activity.findViewById(R.id.tv_contact_details);
        tvPhoneNumber = activity.findViewById(R.id.tv_phone_number);
        lnProgress = activity.findViewById(R.id.lnProgress);
        rlMainLayout = activity.findViewById(R.id.rlMainLayout);
        txtPressOkButton = activity.findViewById(R.id.txtPressOkButton);
        btnCompleteProcess = activity.findViewById(R.id.btnCompleteProcess);
        checkBoxEMI = activity.findViewById(R.id.checkBoxEMI);
        checkBoxCash = activity.findViewById(R.id.checkBoxCash);
        checkBoxTwelve = activity.findViewById(R.id.checkBoxTwelve);
        checkBoxThree = activity.findViewById(R.id.checkBoxThree);
        checkBoxNine = activity.findViewById(R.id.checkBoxNine);
        checkBoxSix = activity.findViewById(R.id.checkBoxSix);

        rlMainLayout.setVisibility(GONE);

        wireListeners();

        if (!Settings.canDrawOverlays(activity)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName())
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }

        Intent removeIntent = new Intent(activity, OverlayService.class);
        removeIntent.setAction(OverlayService.ACTION_REMOVE_OVERLAY);
        activity.startService(removeIntent);
    }


    private void wireListeners() {
        checkBoxTwelve.setOnCheckedChangeListener(this);
        checkBoxThree.setOnCheckedChangeListener(this);
        checkBoxCash.setOnCheckedChangeListener(this);
        checkBoxEMI.setOnCheckedChangeListener(this);
        checkBoxSix.setOnCheckedChangeListener(this);
        checkBoxNine.setOnCheckedChangeListener(this);

        btnCompleteProcess.setOnClickListener(this);
        btnCompleteProcess.setOnFocusChangeListener(this);
    }

    /**
     * Shows the EMI selection layout.
     */
    public void showEMILayout() {
        rlMainLayout.setVisibility(VISIBLE);
        rlEMILayout.setVisibility(VISIBLE);
    }

    /**
     * Returns true if the EMI checkbox is currently selected.
     */
    public boolean isEmiSelected() {
        return checkBoxEMI.isChecked();
    }

    /**
     * Returns true if the cash checkbox is currently selected.
     */
    public boolean isCashSelected() {
        return checkBoxCash.isChecked();
    }

    /**
     * Shows the confirmation dialog before completing the process.
     * Invokes {@code UiEventCallback.onCompleteProcessClicked()} on user confirmation.
     */
    public void showConfirmationDialog() {

        customCautionDialog = new CustomCautionDialog(activity, checkBoxEMI.isChecked(), new CustomCautionDialog.OnClickCallBack() {
            @Override
            public void clickListener(boolean isConfirmed, String pin) {
                if (isConfirmed) {
                    callback.onCompleteProcessClicked();
                }
                customCautionDialog.dismiss();
            }
        });

        customCautionDialog.show();
        Window window = customCautionDialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    public void showWarningMessageDialog(String title, String message) {
        Log.d(TAG, "showWarningMessageDialog: 2");
        warningMessageDialog = new WarningMessageDialog(activity, title, message, isConfirmed -> {
            warningMessageDialog.dismiss();
            activity.finishAffinity();

        });
        warningMessageDialog.setCancelable(false);
        warningMessageDialog.show();
        Log.d(TAG, "showWarningMessageDialog: 1");
        Window window = warningMessageDialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.9),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // ─── View.OnFocusChangeListener ────────────────────────────────────────────

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        int id = view.getId();
        if (id == R.id.btnSubmitEMICode) {
            btnSubmitEMICode.setTextColor(
                    activity.getColor(hasFocus ? R.color.black : R.color.light_gray));
        } else if (id == R.id.btnCompleteProcess) {
            if (hasFocus) {
                txtPressOkButton.setVisibility(VISIBLE);
                btnCompleteProcess.setBackground(activity.getDrawable(R.drawable.otp_button));
                btnCompleteProcess.setTextColor(activity.getColor(R.color.black));
            } else {
                txtPressOkButton.setVisibility(View.INVISIBLE);
                btnCompleteProcess.setBackgroundColor(Color.parseColor("#00000000"));
                btnCompleteProcess.setTextColor(activity.getColor(R.color.white));
            }
        }
    }

    // ─── View.OnClickListener ──────────────────────────────────────────────────

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnOpenSettings) {
            callback.onOpenNetworkSettings();
        } else if (id == R.id.btnCompleteProcess) {
            showConfirmationDialog();
        }
    }

    // ─── CompoundButton.OnCheckedChangeListener ────────────────────────────────

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        int id = compoundButton.getId();
        if (id == R.id.checkBoxCash) {
            handleCashChecked(isChecked);
        } else if (id == R.id.checkBoxEMI) {
            handleEmiChecked(isChecked);
        }
    }

    private void handleCashChecked(boolean isChecked) {
        if (checkBoxEMI.isChecked()) {
            checkBoxEMI.setChecked(false);
        } else if (isChecked) {
            btnCompleteProcess.setVisibility(VISIBLE);
            btnCompleteProcess.requestFocus();
        } else if (!checkBoxCash.isChecked() && !checkBoxEMI.isChecked()) {
            btnCompleteProcess.setVisibility(GONE);
        }
    }

    private void handleEmiChecked(boolean isChecked) {
        if (checkBoxCash.isChecked()) {
            checkBoxCash.setChecked(false);
        }
        if (isChecked) {
            if (btnCompleteProcess.getVisibility() == GONE) {
                btnCompleteProcess.setVisibility(VISIBLE);
                btnCompleteProcess.requestFocus();
            }
        } else if (!checkBoxEMI.isChecked() && !checkBoxCash.isChecked()) {
            btnCompleteProcess.setVisibility(GONE);
        }
    }
}
