package com.crux.pratd.travelbphc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Requests extends Fragment {
    DatabaseReference mRef;
    public Requests() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRef=FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view= inflater.inflate(R.layout.fragment_requests, container, false);

        mRef=FirebaseDatabase.getInstance().getReference();

        final LinearLayout linearLayout=view.findViewById(R.id.disp_req);
        final TextView msg=new TextView(getActivity());
        msg.setTextSize(20);
        msg.setTextColor(Color.BLACK);
        msg.setGravity(Gravity.CENTER);
        msg.setText("No requests to show");
        msg.setGravity(Gravity.CENTER);
        FirebaseFirestore.getInstance().collection("requests")
                .whereEqualTo("receiver_id",Profile.getCurrentProfile().getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().getDocuments().size() != 0) {
                            linearLayout.removeAllViews();
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                final String key=doc.get("sender_id").toString();
                                final View child=inflater.inflate(R.layout.individual_req, linearLayout,false);
                                final TextView tv=child.findViewById(R.id.req_message);
                                new GraphRequest(
                                        AccessToken.getCurrentAccessToken(),
                                        "/"+key,
                                        null,
                                        HttpMethod.GET,
                                        new GraphRequest.Callback() {
                                            @Override
                                            public void onCompleted(GraphResponse response) {
                                                try{
                                                    Log.d("json",response.getJSONObject().getString("name"));
                                                    tv.setText(response.getJSONObject().getString("name")+" requested to join your plan.");
                                                    tv.setTextSize(16);
                                                }
                                                catch (Exception e)
                                                {
                                                    Log.d("Json",e.toString());
                                                }
                                            }
                                        }).executeAsync();
                                child.findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        JsonObject json=new JsonObject();
                                        json.addProperty("sender",Profile.getCurrentProfile().getId());
                                        json.addProperty("receiver",key);
                                        json.addProperty("status",true);
                                        ApiInterface  apiInterface=ApiClient.getClient().create(ApiInterface.class);
                                        Call<JsonObject> call=apiInterface.acceptReq(json);
                                        call.enqueue(new Callback<JsonObject>() {
                                            @Override
                                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                Log.d("Response",response.toString());
                                            }
                                            @Override
                                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                                Log.d("Response","Error");
                                            }
                                        });
                                        linearLayout.removeView(child);
                                    }
                                });
                                child.findViewById(R.id.reject).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        JsonObject json=new JsonObject();
                                        json.addProperty("sender",Profile.getCurrentProfile().getId());
                                        json.addProperty("receiver",key);
                                        json.addProperty("status",false);
                                        ApiInterface  apiInterface=ApiClient.getClient().create(ApiInterface.class);
                                        Call<JsonObject> call=apiInterface.acceptReq(json);
                                        call.enqueue(new Callback<JsonObject>() {
                                            @Override
                                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                                Log.d("Response",response.toString());
                                            }
                                            @Override
                                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                                Log.d("Response","Error");
                                            }
                                        });
                                        linearLayout.removeView(child);
                                    }
                                });
                                linearLayout.addView(child);
                            }
                        }
                        else {
                            linearLayout.removeAllViews();
                            linearLayout.addView(msg);
                        }
                    }
                });
        return view;
    }
}
