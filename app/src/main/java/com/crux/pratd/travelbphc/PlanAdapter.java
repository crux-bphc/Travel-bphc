package com.crux.pratd.travelbphc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by pratd on 20-01-2018.
 */

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.MyViewHolder> {

    private List<TravelPlan> plans;
    public PlanAdapter(List<TravelPlan> travelPlans)
    {
        this.plans=travelPlans;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final TravelPlan plan = plans.get(position);
        holder.source.setText(plan.getSource());
        holder.dest.setText(plan.getDest());
        holder.date.setText(plan.getDate());
        holder.time.setText(plan.getTime());
        holder.space_left.setText(plan.getSpace());
        holder.view_travellers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Travel Card","Displaying Travellers");
                final String abc[]=plan.getTravellers().split(",");
                AlertDialog.Builder showtravellers=new AlertDialog.Builder(view.getContext());
                View view1=LayoutInflater.from(view.getContext()).inflate(R.layout.display_travellers,null);
                LinearLayout linearLayout=view1.findViewById(R.id.listView);
                showtravellers.setView(view1);
                showtravellers.setTitle("You are travelling with");
                showtravellers.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                final AlertDialog dialog=showtravellers.create();
                for(int i=1;i<=abc.length;i++)
                {
                    final int z=i;
                    final TextView textView=new TextView(getApplicationContext());
                    textView.setPadding(60,30,0,0);
                    textView.setTextColor(Color.WHITE);
                    new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/"+abc[i-1],
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        try{
                            Log.d("json",response.getJSONObject().getString("name"));
                            textView.setText(response.getJSONObject().getString("name"));
                        }
                        catch (Exception e)
                        {
                            Log.d("Json",e.toString());
                        }
                    }
                    }).executeAsync();
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            Intent browserIntent;
                            try {
                                view.getContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                                browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/"+abc[z-1]));
                            } catch (Exception e) {
                                browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/abc[z-1]"));
                            }
                            view.getContext().startActivity(browserIntent);
                        }
                    });
                    textView.setTextSize(18);
                    linearLayout.addView(textView);
                }
                dialog.show();
            }
        });
        if(plan.getSpace().equals("1")||plan.getSpace().equals("2")||plan.getSpace().equals("0"))
            holder.space_left.setTextColor(Color.rgb(255,0,0));
        else
            holder.space_left.setTextColor(Color.rgb(48,252,3));
        holder.source.setContentDescription(plan.getCreator());
        if(plan.getSource().equalsIgnoreCase("station")||plan.getDest().equalsIgnoreCase("station"))
            holder.background.setBackgroundResource(R.drawable.train);
        else
            holder.background.setBackgroundResource(R.drawable.flight2);
    }
    @Override
    public int getItemCount(){
        return plans.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.travel_card, parent, false);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("clicked",view.findViewById(R.id.from_text).getContentDescription()+"");
                final String creatorId=view.findViewById(R.id.from_text).getContentDescription()+"";
                final AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                if(creatorId.equals(Profile.getCurrentProfile().getId()))
                    builder.setMessage("You cannot join your own plan!");
                else if(((TextView)(view.findViewById(R.id.spaceleft))).getText().toString().equals("0"))
                    builder.setMessage("No Space Left!");
                else
                {
                    builder.setMessage("Do you wish to join the selected Plan?");
                    builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference().child("requests").child(creatorId).child(Profile.getCurrentProfile().getId()).setValue("I would like to join your plan");
                        }
                    });
                }
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();
            }
        });
        return new MyViewHolder(itemView);
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView source,dest,date,time,space_left,view_travellers;
        public ImageView background;

        public MyViewHolder(View view) {
            super(view);
            source = view.findViewById(R.id.from_text);
            dest = view.findViewById(R.id.to_text);
            date =  view.findViewById(R.id.date);
            time=view.findViewById(R.id.time);
            background=view.findViewById(R.id.back_img);
            space_left=view.findViewById(R.id.spaceleft);
            view_travellers=view.findViewById(R.id.viewtravellers);
        }
    }
}
