package com.walton.startupbroadcast.interfaces;

import com.walton.startupbroadcast.retrofit.ResponseData;

public interface ServerResponseSaveData {

    void Success(String response, String limitCounter);
    void Error(String errorMessage);
    void SyncData(ResponseData responseData, String limitCounter);
}
