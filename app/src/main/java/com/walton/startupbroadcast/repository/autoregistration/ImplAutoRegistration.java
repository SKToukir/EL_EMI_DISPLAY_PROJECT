package com.walton.startupbroadcast.repository.autoregistration;

import static com.walton.startupbroadcast.utilities.Config.CONFIG_PATH;
import static com.walton.startupbroadcast.utilities.Config.METHOD_RESULT;
import static com.walton.startupbroadcast.utilities.Config.SET_PHONE_TAG;
import static com.walton.startupbroadcast.utilities.Config.SET_REGISTER_TAG;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.mediatek.twoworlds.factory.MtkTvFApiInformation;
import com.mediatek.twoworlds.factory.MtkTvFApiSystem;
import com.mediatek.twoworlds.factory.MtkTvFApiTv;
import com.walton.startupbroadcast.utilities.Config;

public class ImplAutoRegistration implements IAutoRegistrationRepo{

    public static ImplAutoRegistration instance;

    private Context mContext;

    public ImplAutoRegistration(Context mContext) {
        this.mContext = mContext;
    }

    public static synchronized ImplAutoRegistration getInstance(Context context){
        if (instance == null) {
            instance = new ImplAutoRegistration(context);
        }
        return instance;
    }

    @Override
    public String getMacAddress() {
        return MtkTvFApiSystem.getInstance().getEmmcEnvVar("macaddr");
    }

    @Override
    public void setRegistrationStatus(boolean status) {
       setInfo(SET_REGISTER_TAG,String.valueOf(status));
    }

    @Override
    public boolean isRegistered() {
        String data = getInfo(SET_REGISTER_TAG);
        if (data.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(data);
    }

    @Override
    public void savePhoneNumber(String customerPhoneNumber) {
        setInfo(SET_PHONE_TAG, customerPhoneNumber);
    }

    @Override
    public String getPhoneNumber() {
        String data = getInfo(SET_PHONE_TAG);
        if (!data.isEmpty()) {
            return data;
        }else {
            return  "null";
        }
    }

    private String getInfo(String tag){
        return MtkTvFApiInformation.getInstance().getIniParameter(CONFIG_PATH, tag);
    }

    private void setInfo(String tag,String status) {
        MtkTvFApiInformation.getInstance().setIniParameter(CONFIG_PATH, tag, status);
    }
}
