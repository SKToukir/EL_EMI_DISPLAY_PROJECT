package com.walton.startupbroadcast.helper;

import com.walton.startupbroadcast.pin.PinContext;
import com.walton.startupbroadcast.pin.PinService;
import com.walton.startupbroadcast.repository.autoregistration.ImplAutoRegistration;

import java.util.List;

public class BarcodeProcessor {

    private final PinService pinService;
    private final ImplAutoRegistration autoRegistration;

    public BarcodeProcessor(PinService pinService, ImplAutoRegistration autoRegistration) {
        this.pinService = pinService;
        this.autoRegistration = autoRegistration;
    }

    public BarcodeResult processBarcode(String barcode, String macAddress) {
        // Step 1: generate pass codes
        PinService.Result passCodeResult = pinService.generatePassCodes();
        List<String> passCodeList = passCodeResult.list;
        String passCodeString = passCodeResult.concatenated;

        // Step 2: generate dynamic salt
        String displaySalt = pinService.generateDynamicSalt();
        String salt = pinService.generateDynamicSalt();
        autoRegistration.savePhoneNumber(salt);

        // Step 3: generate PINs
        PinContext context = new PinContext(barcode, macAddress, salt);
        PinService.Result emiPinResult = pinService.generateEmiPins(context);
        List<String> emiPins = emiPinResult.list;
        String emiPinsString = emiPinResult.concatenated;

        // Step 4: generate display pins
        PinContext displayContext = new PinContext(barcode, macAddress, displaySalt);
        PinService.Result displayPinResult = pinService.generateDisplayPins(displayContext);
        List<String> displayPins = displayPinResult.list;
        String displayPinsString = displayPinResult.concatenated;

        // Step 5: generate display pass codes
        PinService.Result displayPassCodes = pinService.generateDisplayPassCodes();
        List<String> displayPassCodeList = displayPassCodes.list;
        String displayPassString = displayPassCodes.concatenated;

        return new BarcodeResult(passCodeList, passCodeString, emiPins, emiPinsString, displayPins, displayPinsString, displayPassCodeList, displayPassString);
    }

    public static class BarcodeResult {
        public final List<String> passCodeList;
        public final String passCodeString;
        public final List<String> emiPinList;
        public final String emiPinString;
        public final List<String> displayPinList;
        public final String displayPinString;
        public final List<String> displayPassCodes;
        public final String displayPassCodesString;

        public BarcodeResult(List<String> passCodeList, String passCodeString,
                             List<String> emiPinList, String emiPinString,
                             List<String> displayPinList, String displayPinString, List<String> displayPassCodeList, String displayPassCodeString) {
            this.passCodeList = passCodeList;
            this.passCodeString = passCodeString;
            this.emiPinList = emiPinList;
            this.emiPinString = emiPinString;
            this.displayPinList = displayPinList;
            this.displayPinString = displayPinString;
            this.displayPassCodes = displayPassCodeList;
            this.displayPassCodesString = displayPassCodeString;
        }
    }
}

