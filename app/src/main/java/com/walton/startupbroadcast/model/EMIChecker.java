package com.walton.startupbroadcast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EMIChecker {

    @SerializedName("result")
    @Expose
    private Boolean result;
    @SerializedName("current_counter")
    @Expose
    private String currentCounter;

    @SerializedName("limit_counter")
    @Expose
    private String limitCounter;

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

    @SerializedName("record")
    @Expose
    private Record record;

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

}