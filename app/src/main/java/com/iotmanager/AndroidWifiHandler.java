package com.iotmanager;

import android.app.ProgressDialog;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;

/**
 * Created by connorstein on 15-05-31.
 * Managing wifi connections of the android device itself.
 *
 */
public class AndroidWifiHandler{
    private static final String TAG="Connors Debug";
    private static final int MAX_NUM_IP_POLLS=100; //100 polls maximum before timeout
    private static final int IP_WAIT_TIME=100; //100ms in between polling for non-zero IP address

    /**
     * Connects the android device to a network.
     * @param network The network object to connect to
     * @param handler Handler to indicate what to do after the android device has connected or failed to connect
     * @return A thread that can be used to connect the device with .start()
     */
    public static Thread connect(final Network network,final Handler handler){

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

    /**
     * Helper function that adds a network configuration to the device, but not necessarily connect
     * @param network The network to add
     * @return true if successfully added, false if not
     */
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

    /**
     * Helper method which disconnects from the current network and actually connects the device
     * @param network
     * @return true if successfully connect, false if not
     */
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

    /**
     * Poll until non-zero IP address (once a non-zero ip address is obtained, device is ready to use the network)
     * Tried to use NetworkInfo.DetailedState to check if it was CONNECTED
     * However the api method said it remained in OBTAINING_IPADDR state even after it obtained an ip (must be bug)
     * @param network
     * @return true when an IP address has been obtained within a reasonable amount of polls, false if timeout
     */
    private static boolean pollForIp(Network network){

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
