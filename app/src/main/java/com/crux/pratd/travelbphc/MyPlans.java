package com.crux.pratd.travelbphc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyPlans extends Fragment {


    DatabaseReference mRef;
    private PlanAdapter adapter;
    private List<TravelPlan> plan_list = new ArrayList<>();
    private RecyclerView recyclerView;
    public MyPlans() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_search,container,false);
        mRef= FirebaseDatabase.getInstance().getReference();

        recyclerView=view.findViewById(R.id.rec_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter = new PlanAdapter(plan_list);
        final TextView recycler_status=view.findViewById(R.id.status);
        mRef.child("plans").child(Profile.getCurrentProfile().getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                plan_list.clear();
                if(dataSnapshot.getValue()!=null)
                {
                    final String plans_id[]=dataSnapshot.getValue().toString().split(",");
                    recyclerView.setAdapter(adapter);
                    for(final String id:plans_id)
                    {
                        mRef.child(id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                TravelPlan p=dataSnapshot.getValue(TravelPlan.class);
                                plan_list.add(p);
                                Log.d("Size",plan_list.size()+"");
                                adapter.notifyDataSetChanged();
                                if(plan_list.size()==0)
                                {
                                    recycler_status.setVisibility(View.VISIBLE);
                                    recycler_status.setText("You haven't joined or created any plan...");
                                }
                                else
                                    recycler_status.setVisibility(View.INVISIBLE);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
    }

}
