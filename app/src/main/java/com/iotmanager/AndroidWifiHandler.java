package com.iotmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
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
    private static final int MAX_NUM_IP_POLLS=2000;

    public static Thread connect(final Network network,final ProgressDialog progressDialog,final Context context,final Handler handler){

        Thread connectThread=new Thread(new Runnable(){
            @Override
            public void run() {
                if(!addConfiguration(network)){
                    Log.i(TAG,"Unable to add network");
                    Toast.makeText(context,"Unable to add network, ensure password is correct",Toast.LENGTH_LONG).show();
                    handler.sendEmptyMessage(0);
                }
                if(!connectHelper(network)){
                    Log.i(TAG,"Unable to connect to network");
                    Toast.makeText(context,"Unable to connect to network, ensure password is correct",Toast.LENGTH_LONG).show();
                    handler.sendEmptyMessage(0);
                }
                if(!pollForIp(network)){
                    Log.i(TAG,"Unable to get an ip");
                    Toast.makeText(context,"Unable to get IP, ensure password is correct",Toast.LENGTH_LONG).show();
                    handler.sendEmptyMessage(0);
                }
                handler.sendEmptyMessage(0);
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
            Log.i(TAG, "IP " + info.getIpAddress());
            try{
                Thread.sleep(100);
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
