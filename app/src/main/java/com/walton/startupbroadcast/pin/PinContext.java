package com.walton.startupbroadcast.pin;

public class PinContext {

    private final String barcode;
    private final String macAddress;
    private final String salt;

    public PinContext(String barcode, String macAddress, String salt) {
        this.barcode = barcode;
        this.macAddress = macAddress;
        this.salt = salt;
    }

    public String getSalt() {
        return salt;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getBarcode() {
        return barcode;
    }

}
