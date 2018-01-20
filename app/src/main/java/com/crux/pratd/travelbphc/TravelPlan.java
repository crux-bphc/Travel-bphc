package com.crux.pratd.travelbphc;

/**
 * Created by pratd on 20-01-2018.
 */

public class TravelPlan {
    private String source,dest,date,time;
    public TravelPlan(){}
    public TravelPlan(String source,String dest,String date,String time){
        this.source=source;
        this.dest=dest;
        this.date=date;
        this.time=time;
    }
    public String getSource(){return source;}
    public String getDate() {return date;}
    public String getDest() {return dest;}
    public String getTime() {return time;}
    public void setDate(String date) {this.date = date;}
    public void setDest(String dest) {this.dest = dest;}
    public void setSource(String source) {this.source = source;}
    public void setTime(String time) {this.time = time;}
}
