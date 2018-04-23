package com.crux.pratd.travelbphc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Search extends Fragment {
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    private PlanAdapter adapter;
    private int y,m,d,h,s;
    private List<TravelPlan> plan_list = new ArrayList<>();
    private List<TravelPlan> plan_list_filtered=new ArrayList<>();
    private RecyclerView recyclerView;
    Calendar myCalendar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myCalendar=Calendar.getInstance();
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_search,container,false);
        mDatabase= FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference();

        //TODO remove the next two lines after creating new activity layout
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child(Profile.getCurrentProfile().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null)
                            Toast.makeText(getActivity(),"You can create only 1 plan at a time!",Toast.LENGTH_LONG).show();
                        else {
                            Intent intent = new Intent(getContext(), CreatePlan.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });
        y=m=d=h=s=-1;
        recyclerView=view.findViewById(R.id.rec_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        final TextView recycler_status=view.findViewById(R.id.status);
        final ProgressBar progress=view.findViewById(R.id.recycler_progress);
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
                    progress.setVisibility(View.GONE);
                    recycler_status.setText("No plans active currently");
                }
                else
                {
                    progress.setVisibility(View.GONE);
                    recycler_status.setVisibility(View.INVISIBLE);
                }
                applyFilter();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.planner, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.filter) {
            Log.d("Opened Dialog","Y"+y+",M"+m+",D"+d+",H"+h+",S"+s);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final View dialogview=getLayoutInflater().inflate(R.layout.apply_filter,null);
            builder.setTitle("Filter");
            builder.setView(dialogview);
            final TextView fil_date=dialogview.findViewById(R.id.filter_date);
            final TextView fil_time=dialogview.findViewById(R.id.filter_time);
            final DatePickerDialog.OnDateSetListener dplistener=new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    fil_date.setText(i2+"/"+(i1+1)+"/"+i);
                    y=i;
                    m=i1;
                    d=i2;
                }
            };
            fil_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog dp=new DatePickerDialog(getActivity(),dplistener,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
                    dp.create();
                    dp.show();
                }
            });
            final TimePickerDialog.OnTimeSetListener tplistener= new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    //fil_time.setText((i<10?"0"+i:(i>12?i-12:i))+":"+(i1<10?"0"+i1:i1)+(i>=12?" PM":" AM"));
                    h=i;
                    s=i1;
                    fil_time.setText((i<10?"0"+i:i)+":"+(i1<10?"0"+i1:i1));
                }
            };
            fil_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TimePickerDialog tpd=new TimePickerDialog(getActivity(),tplistener,12,0,false);
                    tpd.create();
                    tpd.show();
                }
            });
            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d("Apply Filter","Y"+y+",M"+m+",D"+d+",H"+h+",S"+s);
                    if(h==-1) {
                        Toast.makeText(getActivity(), "You haven't set the date", Toast.LENGTH_SHORT).show();
                        y = m = d = h = s = -1;
                    }
                    else if(d==-1)
                    {
                        Toast.makeText(getActivity(),"You haven't set the time",Toast.LENGTH_SHORT).show();
                        y = m = d = h = s = -1;
                    }
                    else
                        applyFilter();
                }
            });
            builder.setNegativeButton("Clear Filter", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d("Clear Filter","Y"+y+",M"+m+",D"+d+",H"+h+",S"+s);
                    if(y!=-1) {
                        y = m = d = h = s = -1;
                        applyFilter();
                    }
                }
            });
            builder.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void applyFilter()
    {
        plan_list_filtered.clear();
        if(d!=-1) {
            Calendar filter=Calendar.getInstance();
            Calendar plan=Calendar.getInstance();
            filter.set(y,m,d,h,s);
            for (TravelPlan p : plan_list) {
                int y1=Integer.parseInt(p.getDate().substring(p.getDate().lastIndexOf('.')+1));
                int m1=Integer.parseInt(p.getDate().substring(p.getDate().indexOf('.')+1,p.getDate().lastIndexOf('.')))-1;
                int d1=Integer.parseInt(p.getDate().substring(0,p.getDate().indexOf('.')));
                int h1=Integer.parseInt(p.getTime().substring(0,2));
                int s1=Integer.parseInt(p.getTime().substring(3,5));
                plan.set(y1,m1,d1,h1,s1);
                if(Math.abs(filter.getTimeInMillis()-plan.getTimeInMillis())<3600000)
                    plan_list_filtered.add(p);
            }
        }
        else {
            plan_list_filtered=new ArrayList<>(plan_list);
            Log.d("Refreshing Recyler",""+plan_list.size()+","+plan_list_filtered.size());
        }
        adapter=new PlanAdapter(plan_list_filtered);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
