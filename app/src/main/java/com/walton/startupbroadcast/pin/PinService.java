package com.walton.startupbroadcast.pin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PinService {

    private static final int RANGE_OF_EMI_MONTHS = 36;
    private static final int RANGE_OF_DISPLAY_PIN = 12;

    private static final String SEPARATOR = "*";

    // Step 1: Generate pass codes (list + concatenated string)
    public Result generatePassCodes() {
        List<String> passCodeList = new ArrayList<>();
        Set<String> uniquePassCodes = new HashSet<>();
        StringBuilder concatenated = new StringBuilder();

        for (int i = 0; i < RANGE_OF_EMI_MONTHS; i++) {
            String code;
            do {
                code = generatePassCode();
            } while (uniquePassCodes.contains(code));
            passCodeList.add(code);
            uniquePassCodes.add(code);

            concatenated.append(code);
            if (i < RANGE_OF_EMI_MONTHS - 1) concatenated.append(SEPARATOR);
        }

        return new Result(passCodeList, concatenated.toString());
    }

    private String generatePassCode() {
        String combined = String.valueOf(new Random().nextInt(65536));
        return sha256(combined).substring(0, 4);
    }

    // Step 2: Generate dynamic salt
    public String generateDynamicSalt() {
        return sha256(String.valueOf(new Random().nextInt(0xFFFF)));
    }

    public String extractPinCode(String code) {
        return code.split("\\s+")[0];
    }

    // Step 3: Generate EMI PINs (list + concatenated string)
    public Result generateEmiPins(PinContext context) {
        List<String> pinCodeList = new ArrayList<>();
        StringBuilder concatenated = new StringBuilder();

        for (int i = 0; i < RANGE_OF_EMI_MONTHS; i++) {
            String pin = generatePin(context, i);
            pinCodeList.add(pin);
            concatenated.append(pin);
            if (i < RANGE_OF_EMI_MONTHS - 1) concatenated.append(SEPARATOR);
        }

        return new Result(pinCodeList, concatenated.toString());
    }

    // Step 3b: Display PINs
    public Result generateDisplayPins(PinContext context) {
        List<String> pinCodeList = new ArrayList<>();
        StringBuilder concatenated = new StringBuilder();

        for (int i = 0; i < RANGE_OF_DISPLAY_PIN; i++) {
            String pin = generatePin(context, i);
            pinCodeList.add(pin);
            concatenated.append(pin);
            if (i < RANGE_OF_DISPLAY_PIN - 1) concatenated.append(SEPARATOR);
        }

        return new Result(pinCodeList, concatenated.toString());
    }

    // Step 3c: Display Passcodes
    public Result generateDisplayPassCodes() {
        List<String> passCodeList = new ArrayList<>();
        Set<String> uniquePassCodes = new HashSet<>();
        StringBuilder concatenated = new StringBuilder();

        for (int i = 0; i < RANGE_OF_DISPLAY_PIN; i++) {
            String code;
            do {
                code = generatePassCode();
            } while (uniquePassCodes.contains(code));
            passCodeList.add(code);
            uniquePassCodes.add(code);

            concatenated.append(code);
            if (i < RANGE_OF_DISPLAY_PIN - 1) concatenated.append(SEPARATOR);
        }

        return new Result(passCodeList, concatenated.toString());
    }

    public String generateHashedPin(String pin, String macAddress) {
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

    private String generatePin(PinContext context, int i) {
        String combined = context.getBarcode() + context.getMacAddress() + context.getSalt() + i;
        return sha256(combined).substring(0, 8);
    }

    // Second hash for saving
    public String generateSecondHash(String pin, String macAddress) {
        String combined = pin + macAddress + pin;
        return sha256(combined).substring(0, 8);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Result container to keep list + concatenated string
    public static class Result {
        public final List<String> list;
        public final String concatenated;

        public Result(List<String> list, String concatenated) {
            this.list = list;
            this.concatenated = concatenated;
        }
    }
}

