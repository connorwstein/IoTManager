package com.example.connorstein.sockethelloworld;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by connorstein on 15-05-31.
 */
public class Connect extends AsyncTask<Object,Void,Boolean>{
    private static final String TAG="sure2015test";
    private Context context;
    private Network network;
    private ProgressDialog progressDialog;
    @Override
    protected Boolean doInBackground(Object... params) {
        network=(Network)params[0];
        context=(Context) params[1];
        progressDialog=(ProgressDialog)params[2];
        final WifiConfiguration conf = new WifiConfiguration();
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
        WifiInfo info=network.manager.getConnectionInfo();
        while((info.getIpAddress())==0){
            //Wait until non-zero IP address (once a non-zero ip address is obtained, you are connected to the network)
            //Tried to use NetworkInfo.DetailedState to check if it was CONNECTED
            //However the api method said it remained in OBTAINING_IPADDR state even after it obtained an ip (must be bug)
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


    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean validNetworkData) {
//        progressDialog.dismiss();
        super.onPostExecute(validNetworkData);
        if(validNetworkData){
            Intent newActivity = new Intent(context, Device.class);
            newActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newActivity.putExtra("Device", network.ssid);
            context.startActivity(newActivity);
        }
        else{
            Toast.makeText(context,"Not valid network parameters, make sure password is correct",Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }


    }
}
