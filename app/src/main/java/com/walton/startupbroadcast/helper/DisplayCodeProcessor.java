package com.walton.startupbroadcast.helper;

import android.content.Context;

import com.walton.startupbroadcast.model.ActivationModel;
import com.walton.startupbroadcast.repository.autoregistration.ImplAutoRegistration;
import com.walton.startupbroadcast.repository.display.ImplIDisplayRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DisplayCodeProcessor {

    private Context mContext;
    private ImplIDisplayRepository implIDisplayRepository;
    private ImplAutoRegistration implAutoRegistration;
    public static DisplayCodeProcessor instance;
    private boolean pinCodeMatched = false;

    public DisplayCodeProcessor(Context context){
        this.mContext = context;
        implAutoRegistration = ImplAutoRegistration.getInstance(mContext);
        implIDisplayRepository = ImplIDisplayRepository.getInstance(mContext);
    }

    public static DisplayCodeProcessor getInstance(Context context){
        if (instance == null){
            instance = new DisplayCodeProcessor(context);
        }
        return instance;
    }

    public boolean isCodeIsValid(String pin){
        String secondHashPin = generateSecondHashPinCode(pin);
        List<ActivationModel> displayPins = implIDisplayRepository.getDisplayPinList();

        for(int i = 0; i < displayPins.size(); i++){
            if (getPinCode(displayPins.get(i).getCode()).equalsIgnoreCase(secondHashPin)){
                if (!displayPins.get(i).isPaid()){
                    displayPins.get(i).setPaid(true);
                    implIDisplayRepository.saveDisplayPins(displayPins);
                    pinCodeMatched = true;
                }else {
                    pinCodeMatched = false;
                }
                break;
            }else {
                pinCodeMatched = false;
            }
        }
        return pinCodeMatched;
    }

    private String generateSecondHashPinCode(String s) {
        String combinedString = s + implAutoRegistration.getMacAddress() + s;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(combinedString.getBytes());

            // Convert hashed bytes to hexadecimal representation
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            // Take the first 8 characters and capitalize them
            return hexString.substring(0, 8).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isPinCodeFinal(String pinCode, List<ActivationModel> activationModelList) {
        ActivationModel lastItem = activationModelList.get(activationModelList.size() - 1);
        /*
         * Updated V3 use getPinCodeMethod
         * */
        if (getPinCode(lastItem.getCode()).equalsIgnoreCase(pinCode)) {
            lastItem.setPaid(true);
            implIDisplayRepository.saveDisplayPins(activationModelList);
            return true;
        }
        return false;
    }

    private String getPinCode(String code) {
        return code.split("\\s+")[0];
    }
}
