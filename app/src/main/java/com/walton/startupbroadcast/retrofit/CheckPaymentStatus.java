package com.walton.startupbroadcast.retrofit;

import android.util.Log;

import com.walton.startupbroadcast.interfaces.IPaymentStatus;
import com.walton.startupbroadcast.model.PaymentStatusChecker;
import com.walton.startupbroadcast.utilities.Config;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckPaymentStatus {

    ApiService apiPayStatusResponse;

    public static CheckPaymentStatus instance;

    public CheckPaymentStatus() {
        apiPayStatusResponse = ApiSmsClient.getApiClient().create(ApiService.class);
    }

    public static synchronized CheckPaymentStatus getInstance() {
        if (instance == null) {
            instance = new CheckPaymentStatus();
        }
        return instance;
    }

    public void checkPaymentStatus(String barcode, IPaymentStatus iPaymentStatus) {
        Call<PaymentStatusChecker> paymentStatusCheckerCall = apiPayStatusResponse.checkPaymentStatus(barcode);

        paymentStatusCheckerCall.enqueue(new Callback<PaymentStatusChecker>() {
            @Override
            public void onResponse(Call<PaymentStatusChecker> call, Response<PaymentStatusChecker> response) {
                if (response.isSuccessful()) {
                    try {
                        Log.d(Config.TAG, "onResponse: 1"+response.body());
                        assert response.body() != null;
                        iPaymentStatus.SellStatus(response.body().getPayment_status());
                        iPaymentStatus.ResultFound(response.body().getResult());
                    } catch (NullPointerException e) {
                        if (iPaymentStatus != null) {
                            iPaymentStatus.Error(e.getLocalizedMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PaymentStatusChecker> call, Throwable t) {
                if (iPaymentStatus != null) {
                    iPaymentStatus.Error(t.getLocalizedMessage());
                }
            }
        });
    }
}
