package com.walton.startupbroadcast.helper;

import android.util.Log;

import com.walton.startupbroadcast.model.ActivationModel;
import com.walton.startupbroadcast.utilities.Config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Handles all PIN and pass code verification logic for EMI unlocking.
 */
public class PinVerificationHelper {

    public interface PinVerificationCallback {
        /** Called when the entered PIN matched a valid unpaid EMI month. */
        void onPinMatched(List<ActivationModel> updatedList);

        /** Called when the final PIN is entered and all EMIs are complete. */
        void onAllEmiComplete();

        /** Called when a PIN matches but the month was already paid. */
        void onAlreadyPaid();

        /** Called when the PIN does not match any entry. */
        void onPinNotMatched();
    }

    private static final String TAG = Config.TAG;

    private final String macAddress;

    public PinVerificationHelper(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Verifies the raw 8-character PIN the user entered against the activation list.
     *
     * @param rawPin              the plain-text PIN from the keyboard input
     * @param activationModelList the stored list of EMI months
     * @param callback            result callback
     */
    public void verify(String rawPin, List<ActivationModel> activationModelList,
                       PinVerificationCallback callback) {
        if (!isValidPin(rawPin)) return;

        String hashedPin = generateHashedPin(rawPin.toLowerCase());
        if (hashedPin == null) return;

        // Check if this is the final/last EMI pin — auto-completes all remaining
        if (isFinalPin(hashedPin, activationModelList)) {
            callback.onAllEmiComplete();
            return;
        }

        // Match against individual months
        boolean matched = false;
        for (ActivationModel model : activationModelList) {
            if (extractPinCode(model.getCode()).equalsIgnoreCase(hashedPin)) {
                matched = true;
                if (!model.isPaid()) {
                    model.setPaid(true);
                    Log.d(TAG, "PinVerificationHelper: pin matched for month code=" + model.getCode());
                    callback.onPinMatched(activationModelList);
                } else {
                    callback.onAlreadyPaid();
                }
                break;
            }
        }

        if (!matched) {
            callback.onPinNotMatched();
        }
    }

    /**
     * Returns the pass code to be displayed on the lock screen (the suffix after the space).
     *
     * @param activationModelList the full activation list
     * @return display pass code string, or empty string if all months are paid
     */
    public String getNextDisplayPassCode(List<ActivationModel> activationModelList) {
        for (ActivationModel model : activationModelList) {
            if (!model.isPaid()) {
                return extractDisplayCode(model.getCode());
            }
        }
        return "";
    }

    /**
     * Checks whether the final EMI in the list matches the given hashed pin.
     * Marks it paid and saves if matched.
     */
    private boolean isFinalPin(String hashedPin, List<ActivationModel> activationModelList) {
        if (activationModelList.isEmpty()) return false;
        ActivationModel lastItem = activationModelList.get(activationModelList.size() - 1);
        if (extractPinCode(lastItem.getCode()).equalsIgnoreCase(hashedPin)) {
            lastItem.setPaid(true);
            return true;
        }
        return false;
    }

    /**
     * Validates basic PIN requirements before hashing.
     */
    private boolean isValidPin(String pin) {
        return pin != null && pin.length() == 8;
    }

    /**
     * Generates SHA-256 hash: SHA256(pin + macAddress + pin), returns first 8 chars uppercase.
     */
    private String generateHashedPin(String pin) {
        String combined = pin + macAddress + pin;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(combined.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hashedBytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.substring(0, 8).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts the PIN portion (before the space) from a stored code string.
     * Format: "PINCODE PASSCODE"
     */
    private String extractPinCode(String code) {
        return code.split("\\s+")[0];
    }

    /**
     * Extracts the display pass code (after the space) from a stored code string.
     * Format: "PINCODE PASSCODE"
     */
    private String extractDisplayCode(String code) {
        return code.substring(code.indexOf(" ") + 1);
    }
}
