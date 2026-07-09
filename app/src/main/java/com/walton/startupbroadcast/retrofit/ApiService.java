package com.walton.startupbroadcast.retrofit;


import com.walton.startupbroadcast.model.CleanChecker;
import com.walton.startupbroadcast.model.CleanStatusModel;
import com.walton.startupbroadcast.model.PaymentStatusChecker;
import com.walton.startupbroadcast.model.SellerInfo;
import com.walton.startupbroadcast.model.WarningMessage;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {


    @GET("logger.php")
    Call<DataModel> getUserInformation(
            @Query("pins") String pinData,
            @Query("mac") String mac,
            @Query("barcode") String barcode,
            @Query("passkeys") String passkeys,
            @Query("display-pin") String displayPin,
            @Query("display-pass") String displayPass
    );

    @GET("logger-checker.php")
    Call<EMIChecker> getEmiStatusResponse(
            @Query("mac") String mac,
            @Query("barcode") String barcode,
            @Query("current_counter") String currentCounter
    );

    @GET("logger-seller.php")
    Call<SellerInfo> getSellerInfo(
            @Query("mac") String mac,
            @Query("barcode") String barcode
    );

    @GET("logger-payment-warning.php")
    Call<WarningMessage> getWarningMessage(
            @Query("mac") String mac,
            @Query("barcode") String barcode
    );

    @GET("logger-cleaner.php")
    Call<CleanChecker> getCleanStatus(
            @Query("mac") String mac
    );

    @GET("logger-cleaner-status.php")
    Call<CleanStatusModel> setCleanStatus(
            @Query("mac") String mac,
            @Query("clean") String status
    );

    @GET("logger-tv-emi-status.php")
    Call<PaymentStatusChecker> checkPaymentStatus(
            @Query("barcode") String barcode
    );
}
