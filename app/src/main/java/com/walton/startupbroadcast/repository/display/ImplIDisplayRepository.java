package com.walton.startupbroadcast.repository.display;

import static com.walton.startupbroadcast.utilities.Config.CONFIG_PATH;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_DISPLAY_COUNTER;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_DISPLAY_PIN;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_DISPLAY_COUNT;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_DISPLAY_PRODUCTION_MODE;
import static com.walton.startupbroadcast.utilities.Config.METHOD_SET_WALTON_DISPLAY_STATUS;
import static com.walton.startupbroadcast.utilities.Config.TAG;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mediatek.twoworlds.factory.MtkTvFApiInformation;
import com.walton.startupbroadcast.model.ActivationModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImplIDisplayRepository implements IDisplayRepository {

    public static ImplIDisplayRepository instance;

    private Context mContext;

    public ImplIDisplayRepository(Context context) {
        this.mContext = context;
    }

    public static synchronized ImplIDisplayRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ImplIDisplayRepository(context);
        }
        return instance;
    }


    @Override
    public void saveTotalDisplayCounter(int counter) {
        setInfo(METHOD_SET_DISPLAY_COUNTER, String.valueOf(counter));
    }

    @Override
    public String getTotalDisplayCounter() {
        String displayCounter = getInfo(METHOD_SET_DISPLAY_COUNTER);
        if (displayCounter.isEmpty()) {
            return String.valueOf(0);
        }
        return displayCounter;
    }

    @Override
    public void saveDisplayPins(List<ActivationModel> pins) {
        setInfo(METHOD_SET_DISPLAY_PIN, new Gson().toJson(pins));
    }

    @Override
    public List<ActivationModel> getDisplayPinList() {

        String displayPins = getInfo(METHOD_SET_DISPLAY_PIN);
        Gson gson = new Gson();
        Type activationListType = new TypeToken<ArrayList<ActivationModel>>() {
        }.getType();
        List<ActivationModel> pinList = gson.fromJson(displayPins, activationListType);
        if (displayPins == null) {
            Log.e(TAG, "getDisplayPins: List is empty");
            return Collections.emptyList();
        }
        return pinList;
    }

    @Override
    public void setDisplayStatus(boolean displayStatus) {
        setInfo(METHOD_SET_WALTON_DISPLAY_STATUS, String.valueOf(displayStatus));
    }

    @Override
    public boolean getDisplayStatus() {
        String displayStatus = getInfo(METHOD_SET_WALTON_DISPLAY_STATUS);
        Log.d(TAG, "getDisplayStatus: " + displayStatus);
        if (displayStatus.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(displayStatus);
    }


    @Override
    public void setDisplayCount(int count) {
        setInfo(METHOD_SET_WALTON_DISPLAY_COUNT, String.valueOf(count));
    }

    @Override
    public String getDisplayCount() {
        String displayCount = getInfo(METHOD_SET_WALTON_DISPLAY_COUNT);
        if (displayCount.isEmpty()) {
            return String.valueOf(0);
        }
        return displayCount;
    }

    @Override
    public void setDisplayProductionMode(boolean isDisplayProductionMode) {
        setInfo(METHOD_SET_WALTON_DISPLAY_PRODUCTION_MODE, String.valueOf(isDisplayProductionMode));
    }

    @Override
    public boolean getDisplayProductionMode() {
        String displayProductionMode = getInfo(METHOD_SET_WALTON_DISPLAY_PRODUCTION_MODE);
        if (displayProductionMode.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(displayProductionMode);
    }


    private String getInfo(String tag) {
        return MtkTvFApiInformation.getInstance().getIniParameter(CONFIG_PATH, tag);
    }

    private void setInfo(String tag, String status) {
        Log.d(TAG, "setInfo: TAG:" + tag + "\n" + "Data:" + status);
        MtkTvFApiInformation.getInstance().setIniParameter(CONFIG_PATH, tag, status);
    }

}
