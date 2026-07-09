package com.walton.startupbroadcast.retrofit;

import com.walton.startupbroadcast.interfaces.ServerResponseCheckEMI;
import com.walton.startupbroadcast.model.CleanChecker;
import com.walton.startupbroadcast.model.CleanStatusModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallCleanStatus {

    ApiService apiCleanStatusResponse;

    public static CallCleanStatus instance;

    public CallCleanStatus(){
        apiCleanStatusResponse = ApiSmsClient.getApiClient().create(ApiService.class);
    }
    public static synchronized CallCleanStatus getInstance() {
        if (instance == null) {
            instance = new CallCleanStatus();
        }
        return instance;
    }


    public void CleanStatusCheck(String macAddress, ServerResponseCheckEMI serverResponseCheckEMI){
        Call<CleanChecker> cleanCheckerCall = apiCleanStatusResponse.getCleanStatus(macAddress);

        cleanCheckerCall.enqueue(new Callback<CleanChecker>() {
            @Override
            public void onResponse(Call<CleanChecker> call, Response<CleanChecker> response) {
                if (serverResponseCheckEMI != null){
                    if (response.isSuccessful()){
                        assert response.body() != null;
                        if (response.body().getIsClean().equalsIgnoreCase("1")){
                            serverResponseCheckEMI.Success("1");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<CleanChecker> call, Throwable t) {
                if (serverResponseCheckEMI != null){
                    serverResponseCheckEMI.Error(t.getLocalizedMessage());
                }
            }
        });
    }

    public void UpdateCleanStatus(String macAddress, String cleanStatus, ServerResponseCheckEMI serverResponseCheckEMI){
        Call<CleanStatusModel> sentStatus = apiCleanStatusResponse.setCleanStatus(macAddress, cleanStatus);

        sentStatus.enqueue(new Callback<CleanStatusModel>() {
            @Override
            public void onResponse(Call<CleanStatusModel> call, Response<CleanStatusModel> response) {
                if (serverResponseCheckEMI != null){
                    if (response.isSuccessful()){
                        if (response.body().getMessage().equalsIgnoreCase("successful")){
                            serverResponseCheckEMI.Success("clean");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<CleanStatusModel> call, Throwable t) {
                if (serverResponseCheckEMI != null){
                    serverResponseCheckEMI.Error(t.getLocalizedMessage());
                }
            }
        });
    }
}
