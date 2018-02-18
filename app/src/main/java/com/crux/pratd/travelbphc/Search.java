package com.crux.pratd.travelbphc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import static com.facebook.FacebookSdk.getApplicationContext;


public class Search extends Fragment {
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    private PlanAdapter adapter;
    private List<TravelPlan> plan_list = new ArrayList<>();
    private RecyclerView recyclerView;
    Calendar myCalendar;
    public Search() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myCalendar=Calendar.getInstance();
        setHasOptionsMenu(true);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.filter) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final View dialogview=getLayoutInflater().inflate(R.layout.apply_filter,null);
            builder.setTitle("Filter");
            builder.setView(dialogview);
            final TextView fil_date=dialogview.findViewById(R.id.filter_date);
            final TextView fil_time=dialogview.findViewById(R.id.filter_time);
            final DatePickerDialog.OnDateSetListener dplistener=new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    fil_date.setText(i2+"/"+(i1+1)+"/"+i);
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
                    fil_time.setText((i<10?"0"+i:(i>12?i-12:i))+":"+(i1<10?"0"+i1:i1)+(i>12?" PM":" AM"));
                    //fil_time.setText((i<10?"0"+i:i)+""+(i1<10?"0"+i1:i1)+" HOURS");
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
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.planner, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
                mRef.child(Profile.getCurrentProfile().getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null)
                            Toast.makeText(getActivity(),"You can create only 1 plan at a time!",Toast.LENGTH_LONG).show();
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
