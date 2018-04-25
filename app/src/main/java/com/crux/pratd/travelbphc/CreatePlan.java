package com.crux.pratd.travelbphc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreatePlan extends AppCompatActivity {

    TextView fil_date,fil_time;
    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);

        fil_date=findViewById(R.id.chooseDate);
        fil_time=findViewById(R.id.chooseTime);
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dplistener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                fil_date.setText(i2+"."+(i1+1)+"."+i);
            }
        };
        fil_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dp=new DatePickerDialog(CreatePlan.this,dplistener,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
                dp.create();
                if(!isFinishing())
                    dp.show();
            }
        });
        final TimePickerDialog.OnTimeSetListener tplistener= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                //fil_time.setText((i<10?"0"+i:(i>12?i-12:i))+":"+(i1<10?"0"+i1:i1)+(i>=12?" PM":" AM"));
                fil_time.setText((i<10?"0"+i:i)+":"+(i1<10?"0"+i1:i1));
            }
        };
        fil_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog tpd=new TimePickerDialog(CreatePlan.this,tplistener,12,0,false);
                tpd.create();
                tpd.show();
            }
        });
        spinner=findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.space_for, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    public void updateDatabase(View view)
    {
        EditText source=findViewById(R.id.textSource);
        EditText destination=findViewById(R.id.textDest);
        if(source.getText().toString().equals(""))
        {
            Toast.makeText(getApplicationContext(),"Source field cannot be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if(destination.getText().toString().equals(""))
        {
            Toast.makeText(getApplicationContext(),"Destination field cannot be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if(fil_date.getText().equals(""))
        {
            Toast.makeText(getApplicationContext(),"You have not chosen a date",Toast.LENGTH_SHORT).show();
            return;
        }
        if(fil_time.getText().equals(""))
        {
            Toast.makeText(getApplicationContext(),"You have not chosen any time",Toast.LENGTH_SHORT).show();
            return;
        }
        final String id=Profile.getCurrentProfile().getId();

        Map<String,Object> abc=new HashMap<>();
        abc.put(id,true);

        TravelPlan create=new TravelPlan(source.getText().toString(),destination.getText().toString(),fil_date.getText().toString(),fil_time.getText().toString(),id,spinner.getSelectedItem().toString(),abc);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("plans").document(id)
                .set(create)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(),"Plan created successfully!",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(CreatePlan.this,plannerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DataUpload", "Error adding document", e);
                        Toast.makeText(getApplicationContext(),"Unable to create plan, try later",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
