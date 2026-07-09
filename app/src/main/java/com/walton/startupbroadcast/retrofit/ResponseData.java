package com.walton.startupbroadcast.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseData {

    @SerializedName("is_locked")
    @Expose
    private boolean isLocked;

    @SerializedName("paid_status")
    @Expose
    private boolean isPaid;

    @SerializedName("on_emi")
    @Expose
    private boolean onEmiOrNot;

    @SerializedName("total_paid_emi")
    @Expose
    private int totalPaidEMI;

    public int getTotalPaidEMI() {
        return totalPaidEMI;
    }

    public void setTotalPaidEMI(int totalPaidEMI) {
        this.totalPaidEMI = totalPaidEMI;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public boolean isOnEmiOrNot() {
        return onEmiOrNot;
    }

    public void setOnEmiOrNot(boolean onEmiOrNot) {
        this.onEmiOrNot = onEmiOrNot;
    }
}
