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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.google.firebase.database.DatabaseReference;
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
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final DatabaseReference mRef=FirebaseDatabase.getInstance().getReference();
        final TravelPlan plan = plans.get(position);
        holder.source.setText(plan.getSource());
        holder.dest.setText(plan.getDest());
        holder.date.setText(plan.getDate());
        holder.time.setText(plan.getTime());
        holder.space_left.setText(plan.getSpace());
        final String travellers_list[]=plan.getTravellers().split(",");
        final View disp[]=new View[travellers_list.length];
        for(int i=1;i<=travellers_list.length;i++)
        {
            final int z=i;
            View v=View.inflate(getApplicationContext(),R.layout.display_travellers,null);
            final TextView textView= v.findViewById(R.id.individual_name);
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/"+travellers_list[i-1],
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
                    Intent browserIntent;
                    try {
                        view.getContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                        browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/"+travellers_list[z-1]));
                    } catch (Exception e) {
                        browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/abc[z-1]"));
                    }
                    view.getContext().startActivity(browserIntent);
                }
            });
            disp[i-1]=v;
        }
        holder.view_travellers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.view_travellers.getText().equals("View Travellers"))
                {
                    for(View list:disp)
                        holder.disp_traveller.addView(list);
                    holder.view_travellers.setText("Hide Travellers");
                }
                else
                {
                    holder.disp_traveller.removeAllViews();
                    holder.view_travellers.setText("View Travellers");
                }
            }
        });

        if(plan.getSpace().equals("1")||plan.getSpace().equals("2")||plan.getSpace().equals("0"))
            holder.space_left.setTextColor(Color.rgb(255,0,0));
        else
            holder.space_left.setTextColor(Color.rgb(48,252,3));
        if(plan.getSource().equalsIgnoreCase("station")||plan.getDest().equalsIgnoreCase("station"))
            holder.background.setBackgroundResource(R.drawable.train);
        else
            holder.background.setBackgroundResource(R.drawable.flight2);
        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String creatorId=plan.getCreator();
                final AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                if(creatorId.equals(Profile.getCurrentProfile().getId()))
                    builder.setMessage("You cannot join your own plan!");
                else if(plan.getSpace().equals("0"))
                    builder.setMessage("No Space Left!");
                else if(checkAlreadyJoined(Profile.getCurrentProfile().getId(),plan.getTravellers()))
                {
                    builder.setMessage("You are already a part of this plan...\nDo you wish to leave?");
                    builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            plan.setSpace(Integer.toString(Integer.parseInt(plan.getSpace())+1));
                            plan.setTravellers(removeId(Profile.getCurrentProfile().getId(),plan.getTravellers()));
                            mRef.child(plan.getCreator()).setValue(plan);
                        }
                    });
                }
                else
                {
                    builder.setMessage("Do you wish to join the selected Plan?");
                    builder.setPositiveButton("Join", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mRef.child("requests").child(creatorId).child(Profile.getCurrentProfile().getId()).setValue("I would like to join your plan");
                            Toast.makeText(getApplicationContext(),"A request has been sent to the creator, you will be notified when your request is accepted.",Toast.LENGTH_LONG).show();
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
    }
    public boolean checkAlreadyJoined(String id, String others)
    {
        String list[]=others.split(",");
        for(int i=0;i<list.length;i++)
            if(list[i].equals(id))
                return true;
        return false;
    }
    public String removeId(String id, String original)
    {

        String edited="";
        String list[]=original.split(",");
        for(int i=0;i<list.length;i++)
            if(!list[i].equals(id))
                edited=edited+list[i]+",";
        edited=edited.substring(0,edited.length()-1);
        return edited;
    }

    @Override
    public int getItemCount(){
        return plans.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.travel_card, parent, false);
        /*itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("clicked",view.findViewById(R.id.from_text).getContentDescription()+"");
                final String creatorId=view.findViewById(R.id.from_text).getContentDescription()+"";
                FirebaseDatabase.getInstance().getReference().child(creatorId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                final AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                if(creatorId.equals(Profile.getCurrentProfile().getId()))
                    builder.setMessage("You cannot join your own plan!");
                else if(((TextView)(view.findViewById(R.id.spaceleft))).getText().toString().equals("0"))
                    builder.setMessage("No Space Left!");
                else if()
                {

                }
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
        });*/
        return new MyViewHolder(itemView);
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView source,dest,date,time,space_left,view_travellers;
        public ImageView background;
        public LinearLayout disp_traveller;

        public MyViewHolder(View view) {
            super(view);
            source = view.findViewById(R.id.from_text);
            dest = view.findViewById(R.id.to_text);
            date =  view.findViewById(R.id.date);
            time=view.findViewById(R.id.time);
            background=view.findViewById(R.id.back_img);
            space_left=view.findViewById(R.id.spaceleft);
            view_travellers=view.findViewById(R.id.viewtravellers);
            disp_traveller=view.findViewById(R.id.listTraveller);
        }
    }
}
