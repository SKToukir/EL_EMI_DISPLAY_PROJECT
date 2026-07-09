package com.walton.startupbroadcast.retrofit;

import static com.walton.startupbroadcast.utilities.Config.TAG;

import android.util.Log;
import android.widget.Toast;

import com.walton.startupbroadcast.interfaces.ServerResponseCheckEMI;
import com.walton.startupbroadcast.interfaces.ServerResponseSaveData;
import com.walton.startupbroadcast.interfaces.ServerResponseSaveDisplayPin;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitDataToServer {

    private ApiService apiInterface;
    private static SubmitDataToServer instance;

    SubmitDataToServer() {
        apiInterface = ApiClient.getApiClient().create(ApiService.class);
    }

    public static synchronized SubmitDataToServer getInstance() {
        if (instance == null) {
            instance = new SubmitDataToServer();
        }
        return instance;
    }

    public void SaveData(String pinCodeString, String passCodeString, String displayPin, String displayPassCodes, String barcode, String mac, ServerResponseSaveData serverResponse) {

        Call<DataModel> call1 = apiInterface.getUserInformation(pinCodeString, mac, barcode, passCodeString, displayPin, displayPassCodes);
        call1.enqueue(new Callback<DataModel>() {
            @Override
            public void onResponse(Call<DataModel> call, Response<DataModel> response) {

                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.body());
                    if (response.body().getResult().equalsIgnoreCase("save_successful")) {
                        Log.d(TAG, "onResponse: ---------------------" + response.body().getResult());
                        serverResponse.Success("success", response.body().getLimitCounter());
                    } else if (response.body().getResult().equalsIgnoreCase("update_successful")) {
                        if (response.body().getResponseData() != null) {
                            Log.d(TAG, "onResponse: ---------------------" + response.body().getResponseData().isPaid());
                            serverResponse.SyncData(response.body().getResponseData(), response.body().getLimitCounter());
                        } else {
                            Log.d(TAG, "onResponse: Device not sold");
                            serverResponse.Success("success", response.body().getLimitCounter());
                            serverResponse.Error("No data found in server!");
                        }
                    } else {
                        Log.d(TAG, "onResponse: --------------------- Try Again");
                        serverResponse.Error("Please try again");
                    }

                    // After registration successful then save the Registration flag and customer number inside system storage
//                    utilityClass.setRegistered(true);
//                    utilityClass.setCustomerNumber(customerPhoneNumber);
//                    showSuccessDialog();
                } else if (response.errorBody() != null) {
                    serverResponse.Error(response.errorBody().toString());
                    Log.d(TAG, "onResponse: " + response.errorBody());
//                    showRegistrationDialog();
//                    Toast.makeText(getApplicationContext(), "1. " + response.errorBody().toString(), Toast.LENGTH_LONG).show();
                } else {
                    serverResponse.Error("Please try again");
//                    Log.d(TAG, "onResponse: 3");
//                    lnProgress.setVisibility(View.INVISIBLE);
//                    otpViewLayout.setVisibility(View.GONE);
//                    rlRegistrationPage.setVisibility(View.VISIBLE);
//                    ok.setClickable(true);
//                    Toast.makeText(getApplicationContext(), "2. " + response.errorBody(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DataModel> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                serverResponse.Error(t.getLocalizedMessage());
//                lnProgress.setVisibility(View.INVISIBLE);
//                otpViewLayout.setVisibility(View.GONE);
//                rlRegistrationPage.setVisibility(View.VISIBLE);
//                ok.setClickable(true);
//                Toast.makeText(getApplicationContext(), "onFailure:" + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
