package com.iotmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by connorstein on 15-07-23.
 * Parser for string responses from the devices, used in the UDP broadcast i.e. the discovery process
 */
public class ResponseParser {
    private static final String TAG="Connors Debug";

    public static Device createDeviceFromResponse(String response){
        return new Device(getDeviceName(response),getDeviceIP(response),getDeviceMAC(response),getDeviceRoom(response),getDeviceType(response));
    }

    public static void removeDuplicates(ArrayList<Device> devices){
        Set<Device>uniqueDevices=new HashSet<>(devices);
        devices.clear();
        devices.addAll(uniqueDevices);
    }

    public static void filterByRoom(ArrayList<Device> devices, String room){
        if(room==null){
            return;
        }
        Log.i(TAG,"Filtering by room: "+room);
        for(Iterator<Device> it=devices.iterator();it.hasNext();){
            Device device=it.next();
            if(!device.getRoom().trim().equals(room.trim())){
                Log.i(TAG,"Removing device: ");
                device.log();
                it.remove();
            }
        }
    }

    public static Intent createIntentForDeviceConfiguration(Device device, Context context){
        Intent i=null;
        switch(device.getType()){
            case "Temperature":
                i=new Intent(context, TemperatureConfiguration.class);
                break;
            case "Lighting":
                i=new Intent(context, LightingConfiguration.class);
                break;
            case "Camera":
                i=new Intent(context, CameraConfiguration.class);
                break;
            case "Heater":
                i=new Intent(context, HeaterConfiguration.class);
                break;
            default:
                Log.i(TAG, "Type not supported");
        }

        i.putExtra("Device",device);
        return i;
    }
    //Takes "Name:...IP:...MAC:..." response and extracts the NAME
    //Assume name does not have IP or MAC in it...
    public static String getDeviceName(String deviceResponse){
        int indexOfName=deviceResponse.indexOf("NAME:");
        int charsInName=5;
        int indexOfIP=deviceResponse.indexOf("IP:");
        return deviceResponse.substring(indexOfName+charsInName,indexOfIP);
    }

    //Takes "Name:...IP:...MAC:..." response and extracts the IP
    //Assume name does not have IP or MAC in it...
    public static String getDeviceIP(String deviceResponse){
        int indexOfIP=deviceResponse.indexOf("IP:");
        int charsInIP=3;
        int indexOfMAC=deviceResponse.indexOf("MAC:");
        return deviceResponse.substring(indexOfIP+charsInIP,indexOfMAC);
    }

    //Takes "Name:...IP:...MAC:..." response and extracts the MAC
    //Assume name does not have IP or MAC in it...
    public static String getDeviceMAC(String deviceResponse){
        int indexOfMAC=deviceResponse.indexOf("MAC:");
        int charsInMac=4;
        int indexOfRoom=deviceResponse.indexOf("ROOM:");
        return deviceResponse.substring(indexOfMAC+charsInMac,indexOfRoom);
    }
    //"Name:....IP:....MAC:...Room:....Type:....
    public static String getDeviceRoom(String deviceResponse){
        int indexOfRoom=deviceResponse.indexOf("ROOM:");
        int charsInRoom=5;
        int indexOfType=deviceResponse.indexOf("TYPE:");
        return deviceResponse.substring(indexOfRoom+charsInRoom,indexOfType);
    }
    public static String getDeviceType(String deviceResponse){
        int indexOfType=deviceResponse.indexOf("TYPE:");
        int charsInType=5;
        return deviceResponse.substring(indexOfType+charsInType);
    }
}
