package com.walton.startupbroadcast.helper;

import static com.walton.startupbroadcast.utilities.Config.PAYMENT_METHOD_CASH;
import static com.walton.startupbroadcast.utilities.Config.PAYMENT_METHOD_INSTALLMENT;

import com.walton.startupbroadcast.model.ActivationModel;
import com.walton.startupbroadcast.pin.PinService;
import com.walton.startupbroadcast.repository.autoregistration.ImplAutoRegistration;
import com.walton.startupbroadcast.repository.display.ImplIDisplayRepository;
import com.walton.startupbroadcast.repository.emi.ImplEMIRepository;
import com.walton.startupbroadcast.retrofit.ResponseData;

import java.util.ArrayList;
import java.util.List;

public class ActivationDataService {
    private final ImplAutoRegistration autoReg;
    private final ImplEMIRepository emiRepo;
    private final ImplIDisplayRepository displayRepo;
    private final PinService pinService;

    public ActivationDataService(ImplAutoRegistration autoReg,
                                 ImplEMIRepository emiRepo,
                                 ImplIDisplayRepository displayRepo,
                                 PinService pinService) {
        this.autoReg = autoReg;
        this.emiRepo = emiRepo;
        this.displayRepo = displayRepo;
        this.pinService = pinService;
    }

    public void saveActivationData(List<String> passCodes, List<String> emiPins, List<String> displayPins, List<String> displayPassCodeList, String barcode,
                                   String limitCounter, boolean isSync, ResponseData responseData, String response) {
        List<ActivationModel> activationList = new ArrayList<>();
        for (int i = 0; i < passCodes.size(); i++) {
            ActivationModel model = new ActivationModel();
            model.setActivationDate("");
            model.setCode(pinService.generateSecondHash(emiPins.get(i), autoReg.getMacAddress()) + " " + passCodes.get(i));
            model.setPaid(false);
            model.setValidityTimeStamp("");
            activationList.add(model);
        }
        if (!isSync) {
            autoReg.setRegistrationStatus(false);
            emiRepo.saveActivationData(activationList);
            emiRepo.saveBarcode(barcode);
            emiRepo.setStartCounter(0);
            emiRepo.setCounterReset(Integer.parseInt(limitCounter));
        } else {
            emiRepo.saveBarcode(barcode);
            emiRepo.setEMIStatus(responseData.isOnEmiOrNot());
            emiRepo.setShouldShowEMIDialog(responseData.isLocked());
            autoReg.setRegistrationStatus(responseData.isPaid());
            emiRepo.setEMIDuration(String.valueOf(responseData.getTotalPaidEMI()));
            emiRepo.setStartCounter(0);
            emiRepo.setCounterReset(Integer.parseInt(limitCounter));

            if (responseData.isPaid()) {
                emiRepo.savePaymentMethod(PAYMENT_METHOD_CASH);
            } else {
                emiRepo.savePaymentMethod(PAYMENT_METHOD_INSTALLMENT);
            }
        }

        List<ActivationModel> displayList = new ArrayList<>();
        for (int i = 0; i < displayPins.size(); i++) {
            ActivationModel model = new ActivationModel();
            model.setActivationDate("");
            model.setCode(pinService.generateSecondHash(displayPins.get(i), autoReg.getMacAddress()) + " " + displayPassCodeList.get(i));
            model.setPaid(false);
            model.setValidityTimeStamp("");
            displayList.add(model);
        }
        displayRepo.saveDisplayPins(displayList);
        displayRepo.saveTotalDisplayCounter(0);
        displayRepo.setDisplayCount(11);
        displayRepo.setDisplayStatus(true);
        displayRepo.setDisplayProductionMode(true);
    }
}
