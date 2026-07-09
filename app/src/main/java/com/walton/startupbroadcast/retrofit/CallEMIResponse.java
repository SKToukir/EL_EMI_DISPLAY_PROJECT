package com.walton.startupbroadcast.retrofit;

import android.util.Log;

import com.walton.startupbroadcast.interfaces.ISellerInfo;
import com.walton.startupbroadcast.interfaces.IWarningMessage;
import com.walton.startupbroadcast.interfaces.ServerResponseCheckEMI;
import com.walton.startupbroadcast.model.SellerInfo;
import com.walton.startupbroadcast.model.WarningMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallEMIResponse {

    ApiService apiEmiResponseInterface;

    public static CallEMIResponse instance;

    public CallEMIResponse() {
        apiEmiResponseInterface = ApiSmsClient.getApiClient().create(ApiService.class);
    }

    public static synchronized CallEMIResponse getInstance() {
        if (instance == null) {
            instance = new CallEMIResponse();
        }
        return instance;
    }


    public void sellerInfo(String mac, String barcode, ISellerInfo iSellerInfo) {
        Call<SellerInfo> sellerInfoCall = apiEmiResponseInterface.getSellerInfo(mac, barcode);

        sellerInfoCall.enqueue(new Callback<SellerInfo>() {
            @Override
            public void onResponse(Call<SellerInfo> call, Response<SellerInfo> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    iSellerInfo.Success(response.body().isStatus(), response.body().getSalesPoint(),
                            response.body().getPhoneNumber());
                } else {
                    iSellerInfo.Error("Something went wrong!");
                }
            }

            @Override
            public void onFailure(Call<SellerInfo> call, Throwable t) {
                iSellerInfo.Error(t.getLocalizedMessage());
            }
        });
    }

    public void getWarningMessage(String mac, String barcode, IWarningMessage iWarningMessage){
        Call<WarningMessage>  warningMessageCall = apiEmiResponseInterface.getWarningMessage(mac, barcode);
        warningMessageCall.enqueue(new Callback<WarningMessage>() {
            @Override
            public void onResponse(Call<WarningMessage> call, Response<WarningMessage> response) {
                if (response.isSuccessful()){
                    iWarningMessage.onSuccess(response.body());
                }else {
                    iWarningMessage.onError("Something went wrong!");
                }
            }

            @Override
            public void onFailure(Call<WarningMessage> call, Throwable t) {
                iWarningMessage.onError(t.getLocalizedMessage());
            }
        });
    }

    public void emiStatus(String mac, String barcode, String currentCounter, ServerResponseCheckEMI serverResponse) {

        Call<EMIChecker> emiStatusCall = apiEmiResponseInterface.getEmiStatusResponse(mac, barcode, currentCounter);

        emiStatusCall.enqueue(new Callback<EMIChecker>() {
            @Override
            public void onResponse(Call<EMIChecker> call, Response<EMIChecker> response) {
                Log.d("TAG", "onResponse: * " + response.body().getResult());
                if (serverResponse != null) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        if (response.body().getResult()) {
                            Log.d("TAG", "onResponse: 1 * " + response.body().getResult());
                            try {
                                serverResponse.SetCounter(response.body().getCurrentCounter(), response.body().getLimitCounter());
                                serverResponse.Success(response.body().getRecord().getStatus());
                            } catch (NullPointerException e) {
                                serverResponse.Error(e.getMessage());
                            }
                        } else {
                            serverResponse.Error("Device information has not been entered, \nPlease contact the sales team for assistance");
                        }
                    } else {
                        serverResponse.Error("No Successful");
                    }
                } else {
                    serverResponse.Error("No Response from server");
                }
            }

            @Override
            public void onFailure(Call<EMIChecker> call, Throwable t) {
                if (serverResponse != null) {
                    serverResponse.Error(t.getLocalizedMessage());
                }
            }
        });
    }


}
