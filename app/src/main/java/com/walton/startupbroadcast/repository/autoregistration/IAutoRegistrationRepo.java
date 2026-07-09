package com.walton.startupbroadcast.repository.autoregistration;

public interface IAutoRegistrationRepo {
    String getMacAddress();
    void setRegistrationStatus(boolean status);
    boolean isRegistered();
    void savePhoneNumber(String customerPhoneNumber);
    String getPhoneNumber();
}
