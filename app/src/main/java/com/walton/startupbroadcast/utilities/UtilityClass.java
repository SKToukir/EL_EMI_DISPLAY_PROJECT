package com.walton.startupbroadcast.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class UtilityClass {

    @SuppressLint("StaticFieldLeak")
    public static volatile UtilityClass instance;
//    private boolean isRegistered;
//    private String customerNumber;
//    private String macAddress;
//    private Uri uri;
    private Context mContext;
    public UtilityClass(Context context) {
        this.mContext = context;
//        uri = new Uri.Builder().scheme(Constant.CONTENT).authority(Constant.AUTHORITY).build();
    }
    public static UtilityClass getInstance(Context context){
        if (instance == null){
            synchronized (UtilityClass.class){
                if (instance == null){
                    instance = new UtilityClass(context);
                }
            }
        }
        return instance;
    }
//    public boolean isRegistered() {
//        Bundle methodSetTag = mContext.getContentResolver().call(uri, Constant.GET_REGISTER_TAG, "", null);
//        if (methodSetTag != null) {
//            isRegistered = Boolean.parseBoolean(methodSetTag.getString(METHOD_RESULT));
//        }
//        return isRegistered;
//    }
//
//    public void setRegistered(boolean registered) {
//        mContext.getContentResolver().call(uri, Constant.SET_REGISTER_TAG, String.valueOf(registered), null);
//        isRegistered = registered;
//    }
//
//    public String getCustomerNumber() {
//        Bundle methodSetTag = mContext.getContentResolver().call(uri, Constant.GET_PHONE_TAG, "", null);
//        if (methodSetTag != null) {
//            customerNumber = methodSetTag.getString(METHOD_RESULT);
//        }else {
//            customerNumber = "null";
//        }
//        return customerNumber;
//    }
//
//    public void setCustomerNumber(String customerNumber) {
//        mContext.getContentResolver().call(uri, Constant.SET_PHONE_TAG, customerNumber, null);
//        this.customerNumber = customerNumber;
//    }
//
//    public String getMacAddress() {
//        Bundle methodGetMacAddress = mContext.getContentResolver().call(uri, Constant.METHOD_GET_MAC_ADDRESS, null, null);
//        if (methodGetMacAddress != null) {
//            macAddress = methodGetMacAddress.getString(METHOD_RESULT);
//        } else {
//            macAddress = "null";
//        }
//        return macAddress;
//    }


    boolean hasInternet;
    public Boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager
//                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                // update UI / trigger retry logic
            }

            @Override
            public void onLost(Network network) {
                // no network at all
                hasInternet = false;
            }
        });
        return hasInternet;
    }

    public void openNetworkSettings(){
        try {
            ComponentName name = new ComponentName("com.android.tv.settings",
                    "com.android.tv.settings.connectivity.NetworkActivity");
            Intent i=new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            i.setComponent(name);
            mContext.startActivity(i);
        }catch(Exception e){
            mContext.startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    public void restartApp(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName("com.walton.startupbroadcast", "com.walton.startupbroadcast.MainActivity");
        intent.setComponent(cn);
        mContext.startActivity(intent);
    }

    public void hideKeyPad(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
