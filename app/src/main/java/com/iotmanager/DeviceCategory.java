package com.iotmanager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class DeviceCategory extends AppCompatActivity {
    private ListView devicesListView;
    private static final String TAG="Connors Debug";
    private String deviceCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Not this onCreate is only called when coming from the home activity
        //Not called from back button in child activity, see manifest
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_category);
        devicesListView=(ListView)findViewById(R.id.devicesListView);

        switch(getIntent().getStringExtra("Position")){
            case "0":
                deviceCategory="Lighting";
                break;
            case "1":
                deviceCategory="Temperature";
                break;
            default:
                Log.i(TAG,"Error selecting category");

        }
        setTitle(deviceCategory);
        broadcastForDevices(deviceCategory);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.broadcast_for_device_ips:
                broadcastForDevices(deviceCategory);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void broadcastForDevices(String deviceCategory) {
        UdpBroadcast getDevicesInfo= new UdpBroadcast();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Broadcasting for devices");
        progressDialog.setCancelable(false);
        progressDialog.show();
        getDevicesInfo.execute(this,progressDialog,devicesListView,deviceCategory);
    }
}
