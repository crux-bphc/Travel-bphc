package com.crux.pratd.travelbphc;

/**
 * Created by pratd on 20-01-2018.
 */

public class TravelPlan {
    private String source,dest,date,time,creator;
    public TravelPlan(){}
    public TravelPlan(String source,String dest,String date,String time,String creator){
        this.source=source;
        this.dest=dest;
        this.date=date;
        this.time=time;
        this.creator=creator;
    }
    public String getSource(){return source;}
    public String getDate() {return date;}
    public String getDest() {return dest;}
    public String getTime() {return time;}
    public void setDate(String date) {this.date = date;}
    public void setDest(String dest) {this.dest = dest;}
    public void setSource(String source) {this.source = source;}
    public void setTime(String time) {this.time = time;}
    public String getCreator() {return creator;}
    public void setCreator(String creator) {this.creator = creator;}
}
