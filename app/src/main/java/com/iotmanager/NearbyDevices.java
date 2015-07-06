package com.iotmanager;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class NearbyDevices extends AppCompatActivity {

    private static final String TAG="Connors Debug";
    private WifiManager manager;
    private ListView nearbyDevices;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_devices);
        setTitle("Nearby Devices");
        nearbyDevices=(ListView)findViewById(R.id.nearbyDevices);
        manager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        adapter=new ArrayAdapter<String>(
                this,
                R.layout.list,
                getNearbyDevices()
        );
        nearbyDevices.setAdapter(adapter);

    }

    private List<String> getNearbyDevices(){
        boolean scanSuccess=manager.startScan();
        if(!scanSuccess){
            Log.i(TAG,"Unable to scan.");
        }
        List<ScanResult> devices=manager.getScanResults();
        List <String> ssids=new ArrayList<String>();
        for(ScanResult device: devices){
            Log.i(TAG, "Device: " + device.SSID + " RSSI: " + device.level);
            if(device.level>-70) {
                ssids.add(device.SSID + " " +device.level);
            }
        }
        return ssids;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nearby_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()){
            case R.id.actionRefresh:
                adapter.clear();
                adapter.addAll(getNearbyDevices());
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }
}
