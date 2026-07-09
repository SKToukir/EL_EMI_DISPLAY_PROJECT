package com.walton.startupbroadcast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SellerInfo {

    @SerializedName("result")
    @Expose
    private boolean status;
    @SerializedName("sales_point")
    @Expose
    private String salesPoint;
    @SerializedName("phone")
    @Expose
    private String phoneNumber;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getSalesPoint() {
        return salesPoint;
    }

    public void setSalesPoint(String salesPoint) {
        this.salesPoint = salesPoint;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
