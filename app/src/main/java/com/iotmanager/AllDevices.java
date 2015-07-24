package com.iotmanager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.GridView;


public class AllDevices extends AppCompatActivity {

    private static final String TAG="Connors Debug";
    // private GridView deviceCategoryGrid;
    private GridView devicesGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_devices);
        setTitle("All Devices");
        DeviceDBHelper db=new DeviceDBHelper(this);
        db.emptyDB(); //clear out for testing
        devicesGridView=(GridView)findViewById(R.id.deviceCategoryGrid);
        UdpBroadcast deviceBroadcast=new UdpBroadcast();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Broadcasting for devices");
        progressDialog.setCancelable(false);
        progressDialog.show();
        deviceBroadcast.execute(this, progressDialog, devicesGridView, getResources());//will block until devices have been found

    }

    @Override
    protected void onStart() {
        super.onStart();
        devicesGridView.setAdapter(null);
        UdpBroadcast deviceBroadcast=new UdpBroadcast();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Broadcasting for devices");
        progressDialog.setCancelable(false);
        progressDialog.show();
        deviceBroadcast.execute(this, progressDialog, devicesGridView, getResources());//will block until devices have been found

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_devices, menu);
        return true;
    }


}
