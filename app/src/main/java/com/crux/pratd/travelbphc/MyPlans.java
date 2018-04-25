package com.crux.pratd.travelbphc;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import org.w3c.dom.Document;

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
        View view= inflater.inflate(R.layout.fragment_myplans,container,false);
        mRef= FirebaseDatabase.getInstance().getReference();

        view.findViewById(R.id.fab).setVisibility(View.GONE);

        recyclerView=view.findViewById(R.id.rec_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter = new PlanAdapter(plan_list);
        final TextView recycler_status=view.findViewById(R.id.status);
        final ProgressBar progress=view.findViewById(R.id.recycler_progress);

        FirebaseFirestore.getInstance().collection("plans")
                .whereEqualTo("travellers."+Profile.getCurrentProfile().getId(), true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().getDocuments().size()!=0) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                TravelPlan p=doc.toObject(TravelPlan.class);
                                plan_list.add(p);
                            }
                            progress.setVisibility(View.GONE);
                            recycler_status.setVisibility(View.INVISIBLE);
                            adapter=new PlanAdapter(plan_list);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            progress.setVisibility(View.GONE);
                            recycler_status.setVisibility(View.VISIBLE);
                            recycler_status.setText("You haven't joined or created any plan...");
                        }
                    }
                });
        return view;
    }

}
