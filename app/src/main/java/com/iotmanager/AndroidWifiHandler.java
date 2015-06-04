package com.iotmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by connorstein on 15-05-31.
 */
public class AndroidWifiHandler{
    private static final String TAG="Connors Debug";
    private Context context;
    private Network network;
    private boolean initialConfigurationConnect;
    private static final int MAX_NUM_IP_POLLS=100; //50*100 =10 seconds before ip timeout
    private static final int IP_WAIT_TIME=100;
    public static Thread connect(final Network network,final ProgressDialog progressDialog,final Handler handler){

        Thread connectThread=new Thread(new Runnable(){
            @Override
            public void run() {
                Message msg=new Message();
                Bundle bundle=new Bundle();
                if(!addConfiguration(network)){
                    Log.i(TAG,"Unable to add network");
                    bundle.putInt("Error Code",0);

                }
                if(!connectHelper(network)){
                    Log.i(TAG, "Unable to connect to network");
                    bundle.putInt("Error Code",1);

                }
                if(!pollForIp(network)){
                    Log.i(TAG, "Unable to get an ip");
                    bundle.putInt("Error Code",2);
                }
                else{
                    bundle.putInt("Error Code",3);
                }
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
        return connectThread;
    }

    private static boolean addConfiguration(Network network){
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + network.ssid + "\"";
        if(network.password!=null){
            conf.preSharedKey = "\""+ network.password +"\"";
        }
        else{
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        if(network.manager.addNetwork(conf)==-1){
            Log.i(TAG, "Add network fail");
            return false;
        }
        return true;
    }

    private static boolean connectHelper(Network network){
        //Find network in configured networks and connect
        List<WifiConfiguration> configs = network.manager.getConfiguredNetworks();
        for (WifiConfiguration i : configs) {
            if (i.SSID != null && i.SSID.equals("\"" + network.ssid + "\"")) {
                network.manager.disconnect();
                if(network.manager.enableNetwork(i.networkId, true)==false){
                    Log.i(TAG,"Enable Network fail ");
                    return false;
                }
                if(network.manager.reconnect()==false) {
                    Log.i(TAG, "Reconnect fail");
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean pollForIp(Network network){
        //Poll until non-zero IP address (once a non-zero ip address is obtained, you are connected to the network)
        //Tried to use NetworkInfo.DetailedState to check if it was CONNECTED
        //However the api method said it remained in OBTAINING_IPADDR state even after it obtained an ip (must be bug)
        WifiInfo info=network.manager.getConnectionInfo();
        int ipAttempts=0;
        while((info.getIpAddress())==0 && ipAttempts<MAX_NUM_IP_POLLS){
            info=network.manager.getConnectionInfo();
            ipAttempts++;
            Log.i(TAG, "IP " + info.getIpAddress());
            try{
                Thread.sleep(IP_WAIT_TIME);
            }
            catch(InterruptedException e){
                Log.i(TAG,"Interrupted exception");
                return false;
            }
        }
        if(ipAttempts==MAX_NUM_IP_POLLS){
            Log.i(TAG,"IP timeout");
            return false;
        }
        return true;
    }

}
