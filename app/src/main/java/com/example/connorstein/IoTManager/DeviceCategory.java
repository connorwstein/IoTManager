package com.example.connorstein.IoTManager;

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
    private static final String TAG="sure2015test";
    private String deviceType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_category);
        devicesListView=(ListView)findViewById(R.id.devicesListView);

        switch(getIntent().getStringExtra("Position")){
            case "0":
                deviceType="Lighting";
                break;
            case "1":
                deviceType="Temperature";
                break;
            default:
                Log.i(TAG,"Error selecting category");

        }
        setTitle(deviceType);
        broadcastForDevices(deviceType);

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
                broadcastForDevices(deviceType);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void broadcastForDevices(String deviceType) {
        GetIpViaUdpBroadcast getDevicesInfo= new GetIpViaUdpBroadcast();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Broadcasting for devices");
        progressDialog.show();
        getDevicesInfo.execute(this,progressDialog,devicesListView,deviceType);
    }
}
