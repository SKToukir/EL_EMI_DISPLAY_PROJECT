package com.walton.startupbroadcast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CleanChecker {

    @SerializedName("should_clean")
    @Expose
    private String isClean;

    public String getIsClean() {
        return isClean;
    }

    public void setIsClean(String isClean) {
        this.isClean = isClean;
    }

    @SerializedName("message")
    @Expose
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
