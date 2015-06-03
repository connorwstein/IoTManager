package com.iotmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by connorstein on 15-05-31.
 */
public class ConnectAndroid extends AsyncTask<Object,Void,Boolean>{
    private static final String TAG="Connors Debug";
    private Context context;
    private Network network;
    private ProgressDialog progressDialog;
    private boolean initialConfigurationConnect;

    @Override
    protected Boolean doInBackground(Object... args) {
        network=(Network)args[0];
        context=(Context)args[1];
        progressDialog=(ProgressDialog)args[2];
        initialConfigurationConnect=(boolean) args[3];

        if(!addConfiguration(network)){
            //Failed to add the network
            return false;
        }
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
        //Poll until non-zero IP address (once a non-zero ip address is obtained, you are connected to the network)
        //Tried to use NetworkInfo.DetailedState to check if it was CONNECTED
        //However the api method said it remained in OBTAINING_IPADDR state even after it obtained an ip (must be bug)
        WifiInfo info=network.manager.getConnectionInfo();
        while((info.getIpAddress())==0){
            info=network.manager.getConnectionInfo();
            Log.i(TAG, "IP " + info.getIpAddress());
            try{
                Thread.sleep(100);
            }
            catch(InterruptedException e){
                Log.i(TAG,"Interrupted exception");
            }
        }
        return true;
    }

    private boolean addConfiguration(Network network){
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

    @Override
    protected void onPostExecute(Boolean validNetworkData) {
        progressDialog.dismiss();
        super.onPostExecute(validNetworkData);
        if(!validNetworkData){
            Toast.makeText(context,"Invalid network parameters, make sure password is correct. It may be required to reconnect to the device on the previous page.",Toast.LENGTH_LONG).show();
        }
        else if(initialConfigurationConnect){
            Intent newActivity = new Intent(context, InitialDeviceConfiguration.class);
            newActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newActivity.putExtra("Device", network.ssid);
            context.startActivity(newActivity);
        }
        else{
            Toast.makeText(context,"Successful connection",Toast.LENGTH_LONG).show();
            Intent homeActivity= new Intent(context, MainActivity.class);
            homeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(homeActivity);
        }
    }
}
