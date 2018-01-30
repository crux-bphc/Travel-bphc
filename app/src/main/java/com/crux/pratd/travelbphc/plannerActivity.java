package com.crux.pratd.travelbphc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;
import java.util.Calendar;

public class plannerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Profile fbProfile;
    Calendar myCalendar;
    private LayoutInflater lay_inf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planner_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Fragment fragment=new Search();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();

        lay_inf=this.getLayoutInflater();
        fbProfile=Profile.getCurrentProfile();
        setupProfile();

        DrawerLayout drawer =findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu contents=navigationView.getMenu();
        MenuItem mt=contents.getItem(0);
        mt.setChecked(true);
        myCalendar = Calendar.getInstance();

        /*new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/"+fbProfile.getId()+"/groups",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        // Insert your code here
                        Log.d("fbGroup info",response+"");

                    }
                }).executeAsync();*/

        /*TextView reqCount=(TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_req));
        reqCount.setGravity(Gravity.CENTER_VERTICAL);
        reqCount.setText("1");*/
    }

    public void setupProfile()
    {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        TextView uName=hView.findViewById(R.id.userName);
        ImageView iv=hView.findViewById(R.id.userDP);
        Log.d("ProfileLink",fbProfile.getId()+"");
        Picasso.with(getApplicationContext()).load(fbProfile.getProfilePictureUri(100,100)).into(iv);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, fbProfile.getLinkUri());
                startActivity(browserIntent);
            }
        });
        uName.setText(fbProfile.getName());
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.planner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.filter) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View dialogview=lay_inf.inflate(R.layout.apply_filter,null);
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
                    DatePickerDialog dp=new DatePickerDialog(plannerActivity.this,dplistener,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
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
                    TimePickerDialog tpd=new TimePickerDialog(plannerActivity.this,tplistener,12,0,false);
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
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment frag=null;
        if (id == R.id.nav_search) {
            frag=new Search();
        } else if (id == R.id.nav_req) {
            frag=new Requests();
        } else if(id==R.id.time_line){
            frag=new Timeline();
        } else if (id == R.id.log_out) {
            LoginManager.getInstance().logOut();
            Intent intent=new Intent(plannerActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, frag);
        ft.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
