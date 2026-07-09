package com.walton.startupbroadcast.interfaces;

import com.walton.startupbroadcast.model.WarningMessage;

import retrofit2.Response;

public interface IWarningMessage {
    void onSuccess(WarningMessage response);
    void onError(String error);
}
