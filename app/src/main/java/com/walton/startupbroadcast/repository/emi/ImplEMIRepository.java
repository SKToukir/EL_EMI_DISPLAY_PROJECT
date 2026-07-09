package com.walton.startupbroadcast.repository.emi;

import static com.walton.startupbroadcast.utilities.Config.CONFIG_PATH;
import static com.walton.startupbroadcast.utilities.Config.METHOD_GET_WALTON_ACTIVATION;
import static com.walton.startupbroadcast.utilities.Config.METHOD_GET_WALTON_BARCODE;
import static com.walton.startupbroadcast.utilities.Config.METHOD_GET_WALTON_COUNTER_RESET;
import static com.walton.startupbroadcast.utilities.Config.METHOD_GET_WALTON_EMI_DURATION;
import static com.walton.startupbroadcast.utilities.Config.METHOD_GET_WALTON_EMI_STATUS;
import static com.walton.startupbroadcast.utilities.Config.METHOD_GET_WALTON_PAYMENT_METHOD;
import static com.walton.startupbroadcast.utilities.Config.METHOD_GET_WALTON_SHOULD_SHOW;
import static com.walton.startupbroadcast.utilities.Config.METHOD_GET_WALTON_START_COUNTER;
import static com.walton.startupbroadcast.utilities.Config.METHOD_RESULT;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_ACTIVATION;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_ACTIVATION_2;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_BARCODE;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_COUNTER_RESET;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_EMI_DURATION;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_EMI_STATUS;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_PAYMENT_METHOD;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_SHOULD_SHOW;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_START_COUNTER;
import static com.walton.startupbroadcast.utilities.Config.TAG;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mediatek.twoworlds.factory.MtkTvFApiInformation;
import com.walton.startupbroadcast.model.ActivationModel;
import com.walton.startupbroadcast.utilities.Config;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ImplEMIRepository implements IEMIRepository{

    public static ImplEMIRepository instance;
    private Uri uri;
    private Context mContext;
    public ImplEMIRepository(Context context) {
        this.mContext = context;
        uri = new Uri.Builder().scheme(Config.CONTENT).authority(Config.AUTHORITY).build();
    }

    public static synchronized ImplEMIRepository getInstance(Context context){
        if (instance == null){
            instance = new ImplEMIRepository(context);
        }
        return instance;
    }

    @Override
    public void saveActivationData(List<ActivationModel> listOfActivationClass) {

        int mid = listOfActivationClass.size() / 2;
        List<ActivationModel> list1 = listOfActivationClass.subList(0, mid);
        List<ActivationModel> list2 = listOfActivationClass.subList(mid, listOfActivationClass.size());

        setInfo(METHOD_SET_WALTON_ACTIVATION, new Gson().toJson(list1));
        setInfo(METHOD_SET_WALTON_ACTIVATION_2, new Gson().toJson(list2));
    }

    @Override
    public List<ActivationModel> getActivationList() {
        String result1 = getInfo(METHOD_SET_WALTON_ACTIVATION);
        String result2 = getInfo(METHOD_SET_WALTON_ACTIVATION_2);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ActivationModel>>(){}.getType();

        List<ActivationModel> activationList = new ArrayList<>();

        if(result1 != null && !result1.isEmpty()){
            activationList.addAll(gson.fromJson(result1, type));
        }

        if(result2 != null && !result2.isEmpty()){
            activationList.addAll(gson.fromJson(result2, type));
        }

        return activationList;
    }

    @Override
    public void saveBarcode(String barcode) {
        setInfo(METHOD_SET_WALTON_BARCODE, barcode);
    }

    @Override
    public String getTvBarcode() {
        String result = getInfo(METHOD_SET_WALTON_BARCODE);
        if (result.isEmpty()) {
            return "null";
        }
        return result;
    }

    @Override
    public void savePaymentMethod(String paymentMethod) {
        setInfo(METHOD_SET_WALTON_PAYMENT_METHOD, paymentMethod);
    }

    @Override
    public String getPaymentMethod() {
        String result = getInfo(METHOD_SET_WALTON_PAYMENT_METHOD);
        if (result.isEmpty()) {
            return "null";
        }
        return result;
    }

    @Override
    public void setEMIDuration(String emiDuration) {
        setInfo(METHOD_SET_WALTON_EMI_DURATION, emiDuration);
    }

    @Override
    public int getEMIDuration() {
        String result = getInfo(METHOD_SET_WALTON_EMI_DURATION);
        if (result.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(result);
    }

    @Override
    public void setStartCounter(int count) {
        setInfo(METHOD_SET_WALTON_START_COUNTER, String.valueOf(count));
    }

    @Override
    public int getStartCounter() {
        String result = getInfo(METHOD_SET_WALTON_START_COUNTER);
        if (result.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(result);
    }

    @Override
    public void setCounterReset(int counter) {
        setInfo(METHOD_SET_WALTON_COUNTER_RESET,String.valueOf(counter));
    }

    @Override
    public String getCounterReset() {
        String result = getInfo(METHOD_SET_WALTON_COUNTER_RESET);
        if (result.isEmpty()) {
            return String.valueOf(0);
        }
        int k = Integer.parseInt(result);

        return String.valueOf(k);
    }

    @Override
    public void setShouldShowEMIDialog(boolean isShow) {
        setInfo(METHOD_SET_WALTON_SHOULD_SHOW, String.valueOf(isShow));
    }

    @Override
    public boolean shouldShowEMIDialog() {
        String result = getInfo(METHOD_SET_WALTON_SHOULD_SHOW);
        if (result.isEmpty()){
            return false;
        }
        return Boolean.parseBoolean(result);
    }

    @Override
    public void setEMIStatus(boolean status) {
        setInfo(METHOD_SET_WALTON_EMI_STATUS, String.valueOf(status));
    }

    @Override
    public boolean getEMIStatus() {
        String result = getInfo(METHOD_SET_WALTON_EMI_STATUS);
        if (result.isEmpty()){
            return false;
        }
        return Boolean.parseBoolean(result);
    }

    private String getInfo(String tag){
        return MtkTvFApiInformation.getInstance().getIniParameter(CONFIG_PATH, tag);
    }

    private void setInfo(String tag,String status) {
        Log.d(TAG, "setInfo: TAG:"+tag+"\n"+"Data:"+status);
        MtkTvFApiInformation.getInstance().setIniParameter(CONFIG_PATH, tag, status);
    }

}
