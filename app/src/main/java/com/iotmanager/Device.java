package com.iotmanager;

import android.util.Log;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by connorstein on 15-07-28.
 */
public class Device implements Serializable{
    private static final String TAG="Connors Debug";
    private String name;
    private String mac;
    private String ip;
    private String room;
    private String type;

    public Device(String name, String ip, String mac, String room, String type){
        this.name=name;
        this.ip=ip;
        this.mac=mac;
        this.room=room;
        this.type=type;
    }

    public void log(){
        Log.i(TAG, "Device: ("+this.name+", "+this.ip+", "+this.mac+", "+this.room+", "+this.type+")");
    }
    public void setName(String name){
        this.name=name;
    }
    public void setIp(String ip){
        this.ip=ip;
    }
    public void setMac(String mac){
        this.mac=mac;
    }
    public void setRoom(String room){
        this.room=room;
    }
    public void setType(String type){
        this.type=type;
    }

    public String getName(){
        return this.name;
    }
    public String getIp(){
        return this.ip;
    }
    public String getMac(){
        return this.mac;
    }
    public String getRoom(){
        return this.room;
    }
    public String getType(){
        return this.type;
    }

    public int hashCode(){
        //Not very efficient (single bucket), but not a big deal since there is only a few devices
        return 1;
    }

    public boolean equals(Object obj){
        if(obj==null)
            return false;
        else if(!(obj instanceof Device))
            return false;
        else if(((Device) obj).getIp().equals(this.ip)
                &&((Device) obj).getMac().equals(this.mac)
                &&((Device) obj).getName().equals(this.name)
                &&((Device) obj).getRoom().equals(this.room)
                &&((Device) obj).getType().equals(this.type)){
            return true;
        }
        else{
            return false;
        }

    }
}
