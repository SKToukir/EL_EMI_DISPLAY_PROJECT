package com.walton.startupbroadcast.interfaces;

public interface IPaymentStatus {
    void SellStatus(String status);
    void ResultFound(boolean isResultFound);
    void Error(String message);
}
