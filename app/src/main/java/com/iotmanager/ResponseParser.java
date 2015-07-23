package com.iotmanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by connorstein on 15-07-23.
 */
public class ResponseParser {

    //Removes duplicate responses
    //Parses responses for NAME, IP, MAC
    //Returns arraylist of arraylists where list 0: names, list 1: ips, list 3: mac addresses list 4 rooms, list 5 types
    public static ArrayList<ArrayList<String>> getDistinctDeviceInformation(ArrayList<String> deviceResponses){
        ArrayList<String> deviceNames=new ArrayList<String>();
        ArrayList<String> deviceIPs=new ArrayList<String>();
        ArrayList<String> deviceMACs=new ArrayList<String>();
        ArrayList<String> deviceRooms=new ArrayList<String>();
        ArrayList<String> deviceTypes=new ArrayList<String>();
        Set<String> distinctDeviceResponses=new HashSet<>();
        distinctDeviceResponses.addAll(deviceResponses);
        deviceResponses.clear();
        deviceResponses.addAll(distinctDeviceResponses);
        for (String deviceResponse: deviceResponses){
            deviceNames.add(getDeviceName(deviceResponse));
            deviceIPs.add(getDeviceIP(deviceResponse));
            deviceMACs.add(getDeviceMAC(deviceResponse));
            deviceRooms.add(getDeviceRoom(deviceResponse));
            deviceTypes.add(getDeviceType(deviceResponse));
        }
        ArrayList<ArrayList<String>> devicesInformation=new ArrayList<ArrayList<String>>();
        devicesInformation.add(deviceNames);
        devicesInformation.add(deviceIPs);
        devicesInformation.add(deviceMACs);
        devicesInformation.add(deviceRooms);
        devicesInformation.add(deviceTypes);
        return devicesInformation;
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
