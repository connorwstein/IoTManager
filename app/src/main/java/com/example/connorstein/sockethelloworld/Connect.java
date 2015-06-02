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
    private boolean connectAndStartDeviceActivity;

    @Override
    protected Boolean doInBackground(Object... args) {
        network=(Network)args[0];
        context=(Context)args[1];
        progressDialog=(ProgressDialog)args[2];
        connectAndStartDeviceActivity=(boolean) args[3];
        if(!connectAndStartDeviceActivity){
            Log.i(TAG,"Connected android to same network ..");
            //progressDialog.setMessage("Connecting android device to same network ... ");
        }
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
    protected void onPostExecute(Boolean validNetworkData) {
        progressDialog.dismiss();
        super.onPostExecute(validNetworkData);
        if(!validNetworkData){
            Toast.makeText(context,"Invalid network parameters, make sure password is correct. It may be required to reconnect to the device on the previous page.",Toast.LENGTH_LONG).show();
        }
        else if(connectAndStartDeviceActivity){
            Intent newActivity = new Intent(context, InitialDeviceConfiguration.class);
            newActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newActivity.putExtra("Device", network.ssid);
            context.startActivity(newActivity);
        }
        else{
            Toast.makeText(context,"Successful connection",Toast.LENGTH_LONG).show();
        }
    }
}
