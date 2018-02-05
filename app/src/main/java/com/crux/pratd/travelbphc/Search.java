package com.crux.pratd.travelbphc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;


public class Search extends Fragment {
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    private PlanAdapter adapter;
    private List<TravelPlan> plan_list = new ArrayList<>();
    private RecyclerView recyclerView;
    public Search() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_search,container,false);

        mDatabase= FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child("plans").child(Profile.getCurrentProfile().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("CreateNewPlan Check",dataSnapshot.toString());
                        if(dataSnapshot.getValue()!=null)
                            Toast.makeText(getActivity(),"You are aleady a part of a plan so you are not allowed to create a new plan",Toast.LENGTH_LONG).show();
                        else {
                            Intent intent = new Intent(getApplicationContext(), CreatePlan.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        recyclerView=view.findViewById(R.id.rec_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        final TextView recycler_status=view.findViewById(R.id.status);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                plan_list.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    Log.d("key=",ds.getKey());
                    if(ds.getKey().equals("requests")||ds.getKey().equals("plans"))
                        continue;
                    plan_list.add(ds.getValue(TravelPlan.class));
                }
                if(plan_list.size()==0)
                {
                    recycler_status.setVisibility(View.VISIBLE);
                    recycler_status.setText("No plans active currently.");
                }
                else
                {
                    recycler_status.setVisibility(View.INVISIBLE);
                }
                adapter = new PlanAdapter(plan_list);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return view;
    }


}
