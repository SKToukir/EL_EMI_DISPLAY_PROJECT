package com.walton.startupbroadcast.repository.display;

import com.walton.startupbroadcast.model.ActivationModel;

import java.util.List;

public interface IDisplayRepository {

    void saveTotalDisplayCounter(int counter);
    String getTotalDisplayCounter();

    void saveDisplayPins(List<ActivationModel> pins);
    List<ActivationModel> getDisplayPinList();

    void setDisplayStatus(boolean displayStatus);
    boolean getDisplayStatus();


    void setDisplayCount(int count);
    String getDisplayCount();

    void setDisplayProductionMode(boolean isDisplayProductionMode);
    boolean getDisplayProductionMode();
}
