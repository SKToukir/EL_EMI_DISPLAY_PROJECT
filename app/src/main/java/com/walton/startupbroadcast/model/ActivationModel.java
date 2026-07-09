package com.walton.startupbroadcast.model;

public class ActivationModel {
    private String code;
    private String validityTimeStamp;
    private boolean isPaid;
    private String activationDate;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValidityTimeStamp() {
        return validityTimeStamp;
    }

    public void setValidityTimeStamp(String validityTimeStamp) {
        this.validityTimeStamp = validityTimeStamp;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public String getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }
}
