package com.walton.startupbroadcast.utilities.emi;

public class EMIChecker {

    private static EMIChecker instance;

    public static synchronized EMIChecker getInstance() {
        if (instance == null){
            instance = new EMIChecker();
        }
        return instance;
    }

    public boolean isSetPaymentMethod(){
        // Check from system storage is EMI or payment method set or not
        return false;
    }

    public void setPaymentMethodIsEMI(boolean isSet){
        // Save payment method isSet or not in system storage
    }
}
