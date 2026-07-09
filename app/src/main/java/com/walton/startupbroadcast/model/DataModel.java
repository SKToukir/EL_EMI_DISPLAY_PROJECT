package com.walton.startupbroadcast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.walton.startupbroadcast.retrofit.ResponseData;

public class DataModel {
    @SerializedName("result")
    @Expose
    private String result;

    @SerializedName("current_counter")
    @Expose
    private String currentCounter;

    public String getCurrentCounter() {
        return currentCounter;
    }

    public void setCurrentCounter(String currentCounter) {
        this.currentCounter = currentCounter;
    }

    public String getLimitCounter() {
        return limitCounter;
    }

    public void setLimitCounter(String limitCounter) {
        this.limitCounter = limitCounter;
    }

    @SerializedName("limit_counter")
    @Expose
    private String limitCounter;

    @SerializedName("data")
    @Expose
    private ResponseData responseData;

    public ResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }

    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }
}
