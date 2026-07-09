package com.walton.startupbroadcast.repository.emi;

import com.walton.startupbroadcast.model.ActivationModel;

import java.util.List;

public interface IEMIRepository {

    void saveActivationData(List<ActivationModel> listOfActivationClass);
    List<ActivationModel> getActivationList();
    void saveBarcode(String barcode);
    String getTvBarcode();
    void savePaymentMethod(String paymentMethod);
    String getPaymentMethod();
    void setEMIDuration(String emiDuration);
    int getEMIDuration();
    void setStartCounter(int count);
    int getStartCounter();
    void setCounterReset(int counter);
    String getCounterReset();
    void setShouldShowEMIDialog(boolean isShow);
    boolean shouldShowEMIDialog();
    void setEMIStatus(boolean status);
    boolean getEMIStatus();
}
