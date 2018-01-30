package com.crux.pratd.travelbphc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

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
        TravelPlan plan = plans.get(position);
        holder.source.setText(plan.getSource());
        holder.dest.setText(plan.getDest());
        holder.date.setText(plan.getDate());
        holder.time.setText(plan.getTime());
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
        public TextView source,dest,date,time;
        public ImageView background;

        public MyViewHolder(View view) {
            super(view);
            source = view.findViewById(R.id.from_text);
            dest = view.findViewById(R.id.to_text);
            date =  view.findViewById(R.id.date);
            time=view.findViewById(R.id.time);
            background=view.findViewById(R.id.back_img);
        }
    }
}
