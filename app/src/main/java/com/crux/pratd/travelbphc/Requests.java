package com.crux.pratd.travelbphc;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Requests extends Fragment {
    DatabaseReference mRef;
    public Requests() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("oncreate","called");
        super.onCreate(savedInstanceState);
        mRef=FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mRef=FirebaseDatabase.getInstance().getReference();

        final View view= inflater.inflate(R.layout.fragment_requests, container, false);
        final LinearLayout linearLayout=view.findViewById(R.id.disp_req);
        mRef.child("requests").child(Profile.getCurrentProfile().getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                {
                    for(DataSnapshot ds:dataSnapshot.getChildren())
                    {
                        final String key=ds.getKey();
                        Log.d("request",ds.getValue()+"");
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
                                mRef.child("requests").child(Profile.getCurrentProfile().getId()).child(key).setValue(null);
                                mRef.child("plans").child(key).setValue(Profile.getCurrentProfile().getId());
                                mRef.child(Profile.getCurrentProfile().getId()).child("travellers").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot==null)
                                            return;
                                        String s=dataSnapshot.getValue().toString()+","+key;
                                        mRef.child(Profile.getCurrentProfile().getId()).child("travellers").setValue(s);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                linearLayout.removeView(child);
                            }
                        });
                        child.findViewById(R.id.reject).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mRef.child("requests").child(Profile.getCurrentProfile().getId()).child(key).setValue(null);
                                linearLayout.removeView(child);
                            }
                        });
                        linearLayout.addView(child);
                    }
                }
                else{
                    TextView msg=new TextView(getActivity());
                    msg.setTextSize(25);
                    msg.setText("No requests to show...");
                    linearLayout.addView(msg);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }
}
