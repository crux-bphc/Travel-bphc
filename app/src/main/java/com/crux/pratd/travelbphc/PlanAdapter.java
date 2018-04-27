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
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        Set<String> listTravellers=plan.getTravellers().keySet();
        final View disp[]=new View[listTravellers.size()];
        int i=0;
        for(final String id:listTravellers)
        {
            View v=View.inflate(getApplicationContext(),R.layout.display2,null);
            final TextView textView= v.findViewById(R.id.individual_name);
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/"+id,
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            try{
                                textView.setText(response.getJSONObject().getString("name"));
                            }
                            catch (Exception e) {}
                        }
                    }).executeAsync();
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent browserIntent;
                    try {
                        view.getContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                        browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/"+id));
                    } catch (Exception e) {
                        browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"+id));
                    }
                    view.getContext().startActivity(browserIntent);
                }
            });
            disp[i++]=v;
        }
        final LinearLayout container=new LinearLayout(holder.view_travellers.getContext());
        container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setOrientation(LinearLayout.VERTICAL);
        for(View list:disp) {
            container.addView(list);
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(holder.view_travellers.getContext());
        builder.setView(container);
        final AlertDialog dialog=builder.create();
        holder.view_travellers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        if(plan.getSpace().equals("1")||plan.getSpace().equals("2")||plan.getSpace().equals("0")){
            holder.space_left.setTextColor(Color.rgb(255,0,0));
            holder.indicator.setBackgroundColor(Color.rgb(255,0,0));
        }
        else {
            holder.space_left.setTextColor(Color.rgb(48, 252, 3));
            holder.indicator.setBackgroundColor(Color.rgb(48, 252, 3));
        }
        if(plan.getSource().equalsIgnoreCase("station")||plan.getDest().equalsIgnoreCase("station"))
            holder.background.setImageResource(R.drawable.train);
        else
            holder.background.setImageResource(R.drawable.flight);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String creatorId=plan.getCreator();
                final AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                if(creatorId.equals(Profile.getCurrentProfile().getId()))
                    builder.setMessage("You cannot join your own plan!");
                else if(plan.getSpace().equals("0"))
                    builder.setMessage("No Space Left!");
                else if ( plan.getTravellers().containsKey(Profile.getCurrentProfile().getId()))
                {
                    builder.setMessage("You are already a part of this plan...\nDo you wish to leave?");
                    builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JsonObject json=new JsonObject();
                            json.addProperty("sender",Profile.getCurrentProfile().getId());
                            json.addProperty("plan_id",plan.getCreator());
                            ApiInterface  apiInterface=ApiClient.getClient().create(ApiInterface.class);
                            Call<JsonObject> call=apiInterface.leavePlan(json);
                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    Log.d("Response",response.toString());
                                }
                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Log.d("Response","Error");
                                }
                            });
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
                            JsonObject json=new JsonObject();
                            json.addProperty("sender",Profile.getCurrentProfile().getId());
                            json.addProperty("receiver",creatorId);
                            ApiInterface  apiInterface=ApiClient.getClient().create(ApiInterface.class);
                            Call<JsonObject> call=apiInterface.sendReq(json);
                            call.enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                    Log.d("Response",response.toString());
                                }
                                @Override
                                public void onFailure(Call<JsonObject> call, Throwable t) {
                                    Log.d("Response","Unable to process your request");
                                }
                            });
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

    @Override
    public int getItemCount(){
        return plans.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.travel_card_new, parent, false));
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView source,dest,date,time,space_left,view_travellers;
        public ImageView background;
        public View card;
        public View indicator;

        public MyViewHolder(View view) {
            super(view);
            source = view.findViewById(R.id.from_text);
            dest = view.findViewById(R.id.to_text);
            date =  view.findViewById(R.id.date);
            time=view.findViewById(R.id.time);
            background=view.findViewById(R.id.back_img);
            space_left=view.findViewById(R.id.spaceleft);
            view_travellers=view.findViewById(R.id.viewtravellers);
            indicator=view.findViewById(R.id.indicator);
            card=view.findViewById(R.id.cardView);
        }
    }
}
