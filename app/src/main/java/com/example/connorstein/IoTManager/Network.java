package com.example.connorstein.IoTManager;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by connorstein on 15-05-31.
 */
public class Network {
    public String ssid=null;
    public WifiManager manager=null;
    private static final String TAG="sure2015test";
    public String password=null;

    public Network(String ssid, Context context){
        this.ssid=ssid;
        manager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }
    public void setPassword(String password){
        this.password=password;
    }
    public boolean hasPassword(){
        manager.startScan();
        List<ScanResult> results=manager.getScanResults();
        for(ScanResult i:results){
            if(i.capabilities.equals("[ESS]")&&i.SSID.equals(ssid)){
                return false;
            }
        }
        return true;
    }
    public boolean isEnterprise(){
        manager.startScan();
        List<ScanResult>results=manager.getScanResults();
        for(ScanResult i:results){
            if(i.capabilities.contains("WPA")&&(!i.capabilities.contains("PSK"))&&i.SSID.equals(ssid)){
                return true;
            }
        }
        return false;
    }
}
