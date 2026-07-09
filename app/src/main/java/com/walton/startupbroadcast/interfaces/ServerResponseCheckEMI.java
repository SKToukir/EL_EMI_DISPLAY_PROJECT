package com.walton.startupbroadcast.interfaces;

public interface ServerResponseCheckEMI {

    void Success(String response);
    void Error(String errorMessage);
    void SetCounter(String current_counter, String limit_counter);
}
