package com.crux.pratd.travelbphc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
public class CreatePlan extends AppCompatActivity {

    TextView fil_date,fil_time;
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
                fil_time.setText((i<10?"0"+i:(i>12?i-12:i))+":"+(i1<10?"0"+i1:i1)+(i>12?" PM":" AM"));
                //fil_time.setText((i<10?"0"+i:i)+""+(i1<10?"0"+i1:i1)+" HOURS");
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
    }
    public void updateDatabase(View view)
    {
        DatabaseReference dataRef=FirebaseDatabase.getInstance().getReference();
        EditText source=findViewById(R.id.textSource);
        EditText destination=findViewById(R.id.textDest);
        TravelPlan create=new TravelPlan(source.getText().toString(),destination.getText().toString(),fil_date.getText().toString(),fil_time.getText().toString());
        dataRef.child(Profile.getCurrentProfile().getId()).setValue(create);
        Toast.makeText(getApplicationContext(),"Plan created successfully!!",Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(CreatePlan.this,plannerActivity.class);
        startActivity(intent);
        finish();
    }
}
