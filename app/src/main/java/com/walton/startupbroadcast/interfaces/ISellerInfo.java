package com.walton.startupbroadcast.interfaces;

import okhttp3.Response;

public interface ISellerInfo {
    void Success(boolean status, String sellsPointName, String contact);
    void Error(String error);
}
