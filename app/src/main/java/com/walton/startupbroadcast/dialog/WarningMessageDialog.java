package com.walton.startupbroadcast.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.walton.startupbroadcast.R;
import com.walton.startupbroadcast.utilities.FontManager;

public class WarningMessageDialog extends Dialog implements View.OnClickListener {


    private Context mContext;
    private String messsage, title;
    private Button btnConfirm, btnCancel;
    private WarningMessageDialog.OnClickCallBack onClickCallBack;
    private TextView txtBody, txtTitle;

    public WarningMessageDialog(@NonNull Context context, String title, String message, WarningMessageDialog.OnClickCallBack clickCallBack) {
        super(context);
        this.onClickCallBack = clickCallBack;
        this.mContext = context;
        this.messsage = message;
        this.title = title;
    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.warning_message_layout);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        txtBody = findViewById(R.id.txtMessageWarning);
        btnCancel = findViewById(R.id.btnCancelWarning);
        txtTitle = findViewById(R.id.txtSubtitle);

        FontManager.applyFont(btnCancel, getContext(), "bangla_button_font.ttf");

        btnCancel.setOnClickListener(this);

        btnCancel.requestFocus();
        btnCancel.setFocusableInTouchMode(true);
        btnCancel.setDefaultFocusHighlightEnabled(true);

        updatePaymentMethodTextField();

    }

    private void updatePaymentMethodTextField() {
        txtBody.setText(messsage);
        txtTitle.setText(title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCancelWarning:
                if (onClickCallBack != null) {
                    onClickCallBack.clickListener(false);
                }
                break;
        }
    }


    public interface OnClickCallBack {
        void clickListener(boolean isConfirmed);
    }
}
