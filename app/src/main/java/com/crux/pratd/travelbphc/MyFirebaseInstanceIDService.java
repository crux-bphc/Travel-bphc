package com.crux.pratd.travelbphc;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private String token;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        token = FirebaseInstanceId.getInstance().getToken();
        Log.d("Service", "Refreshed token: " + token);
        sendRegistrationToServer();
    }
    public void sendRegistrationToServer(){
        if(token==null)
            return;
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        JSONObject json=new JSONObject();
        try{
            json.put("token",token);
        }
        catch (Exception e){
            Log.d("Notification Service",e.toString());
        }
        Call<JSONObject> call = apiService.sendToken(json);
        call.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                Log.d("Notification Service","Response:"+response.toString());
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {

            }
        });
    }
}
