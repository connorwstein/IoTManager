package com.example.connorstein.sockethelloworld;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;


public class DeviceCategory extends AppCompatActivity {
    private ListView devicesListView;
    private static final String TAG="sure2015test";
    private String category;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_category);
        devicesListView=(ListView)findViewById(R.id.devicesListView);
        switch(getIntent().getStringExtra("Position")){
            case "0":
                category="Lighting Devices";
                break;
            case "1":
                category="Temperature Devices";
                break;
            default:
                Log.i(TAG,"Error selecting category");

        }
        setTitle(category);
        broadcastForDevices();

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
                broadcastForDevices();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void broadcastForDevices() {
        GetIpViaUdpBroadcast getDevicesInfo= new GetIpViaUdpBroadcast();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Broadcasting for devices");
        progressDialog.show();
        getDevicesInfo.execute(this,progressDialog,devicesListView);
    }
}
