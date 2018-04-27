package com.crux.pratd.travelbphc;

import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
        if(Profile.getCurrentProfile()==null)
            return;
        Map<String,String> map=new HashMap<>();
        map.put("token",token);
        FirebaseFirestore.getInstance().collection("user").document(Profile.getCurrentProfile().getId()).set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Token Service","Updated Successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Token Service","Updated Failed");
                    }
                });
    }
}
