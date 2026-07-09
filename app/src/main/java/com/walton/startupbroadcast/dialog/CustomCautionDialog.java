package com.walton.startupbroadcast.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.walton.startupbroadcast.R;
import com.walton.startupbroadcast.utilities.FontManager;

public class CustomCautionDialog extends Dialog implements View.OnClickListener, View.OnFocusChangeListener {


    private Context mContext;
    private Button btnConfirm, btnCancel;
    private OnClickCallBack onClickCallBack;
    private boolean isEMI;
    private TextView txtBody;
    private EditText et_enter_pin;

    public CustomCautionDialog(@NonNull Context context, boolean isEMISelected, OnClickCallBack clickCallBack) {
        super(context);
        this.onClickCallBack = clickCallBack;
        this.mContext = context;
        this.isEMI = isEMISelected;
    }


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_alert_layout);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        et_enter_pin = findViewById(R.id.et_enter_pin);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);
        txtBody = findViewById(R.id.txtBodyDialog);

        FontManager.applyFont(btnCancel,getContext(), "bangla_button_font.ttf");
        FontManager.applyFont(btnConfirm, getContext(), "bangla_button_font.ttf");

        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        btnCancel.setOnFocusChangeListener(this);
        btnConfirm.setOnFocusChangeListener(this);

        btnCancel.setDefaultFocusHighlightEnabled(true);

        updatePaymentMethodTextField();

    }

    private void updatePaymentMethodTextField() {
        if (isEMI){
            txtBody.setText(R.string.emi_selected);
        }else {
            txtBody.setText(R.string.cash_selected);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCancel:
                if (onClickCallBack != null){
                    onClickCallBack.clickListener(false, "");
                }
                break;
            case R.id.btnConfirm:
                if (onClickCallBack != null){
                    onClickCallBack.clickListener(true, et_enter_pin.getText().toString().trim());
                }
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()){
            case R.id.btnCancel:
                if (b){
                    btnCancel.setBackgroundResource(R.drawable.selected_background);
                }else {
                    btnCancel.setBackgroundResource(R.drawable.unselected_button);
                }
                break;
            case R.id.btnConfirm:
                if (b){
                    btnConfirm.setBackgroundResource(R.drawable.selected_background);
                }else {
                    btnConfirm.setBackgroundResource(R.drawable.unselected_button);
                }
                break;
        }
    }

    public interface OnClickCallBack{
        void clickListener(boolean isConfirmed, String pin);
    }
}
