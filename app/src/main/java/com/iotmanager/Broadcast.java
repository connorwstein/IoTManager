package com.iotmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.widget.GridView;

/**
 * Created by connorstein on 15-07-28.
 */
public class Broadcast {

    public static void broadcastForDevices(Context context, GridView gridViewToBeFilled, Resources resources, String roomFilter){
        UdpBroadcast deviceBroadcast=new UdpBroadcast();
        ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Broadcasting for devices");
        progressDialog.setCancelable(false);
        progressDialog.show();
        deviceBroadcast.execute(context, progressDialog, gridViewToBeFilled, resources,roomFilter);//will block until devices have been found
    }

}
