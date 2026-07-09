package com.walton.startupbroadcast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WarningMessage {

    @SerializedName("result")
    @Expose
    private boolean status;
    @SerializedName("warning")
    @Expose
    private boolean warningStatus;
    @SerializedName("warning_level")
    @Expose
    private String warning_level;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("reference_label")
    @Expose
    private String reference_label;
    @SerializedName("reference_date")
    @Expose
    private String reference_date;
    @SerializedName("next_due_date")
    @Expose
    private String next_due_date;
    @SerializedName("days_passed")
    @Expose
    private int days_passed;
    @SerializedName("days_remaining")
    @Expose
    private int days_remaining;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(boolean warningStatus) {
        this.warningStatus = warningStatus;
    }

    public String getWarning_level() {
        return warning_level;
    }

    public void setWarning_level(String warning_level) {
        this.warning_level = warning_level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReference_label() {
        return reference_label;
    }

    public void setReference_label(String reference_label) {
        this.reference_label = reference_label;
    }

    public String getReference_date() {
        return reference_date;
    }

    public void setReference_date(String reference_date) {
        this.reference_date = reference_date;
    }

    public String getNext_due_date() {
        return next_due_date;
    }

    public void setNext_due_date(String next_due_date) {
        this.next_due_date = next_due_date;
    }

    public int getDays_passed() {
        return days_passed;
    }

    public void setDays_passed(int days_passed) {
        this.days_passed = days_passed;
    }

    public int getDays_remaining() {
        return days_remaining;
    }

    public void setDays_remaining(int days_remaining) {
        this.days_remaining = days_remaining;
    }
}
