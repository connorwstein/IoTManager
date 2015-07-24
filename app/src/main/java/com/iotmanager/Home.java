package com.iotmanager;

import android.content.Context;
import android.content.Intent;
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
import java.util.HashMap;
import java.util.List;

public class Home extends AppCompatActivity {
    private static final String TAG="Connors Debug";



    private WifiManager manager;
    private ListView nearbyDevices;
    private ArrayAdapter<String> adapter;
    private DeviceDBHelper deviceDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        deviceDBHelper=new DeviceDBHelper(Home.this);
        nearbyDevices=(ListView)findViewById(R.id.nearbyDevices);
        manager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        setTitle("Room: " +getRoom());
//        adapter=new ArrayAdapter<String>(
//                this,
//                R.layout.list,
//                getNearbyDevices()
//        );
//        nearbyDevices.setAdapter(adapter);
    }
    private String getRoom(){
        boolean scanSuccess=manager.startScan();
        if(!scanSuccess){
            Log.i(TAG,"Unable to scan.");
        }
        HashMap<String,Integer> rooms= new HashMap();
        List<ScanResult> devices=manager.getScanResults();
        for(ScanResult device: devices){
            String ssid=device.SSID;
            if(ssid.contains("ESP")&&ssid.length()==21){ //length ensures that it is a locator device with ESP_MACADDRESS.. and not ESP_somethingelse
                String mac=getMacFromSSID(ssid);
                String room=deviceDBHelper.getRoomFromMac(mac);
                Log.i(TAG,"MAC Found: "+mac+" in room "+room);
                if(rooms.containsKey(room)){
                    rooms.put(room,rooms.get(room)+1);
                }
                else{
                    rooms.put(room,1);
                }
            }
        }
        deviceDBHelper.dumpDBtoLog();
        HashMap.Entry<String,Integer>maxEntry=null;
        //Note that if two rooms have the same amount of detected locators, then it will return the first room
        for(HashMap.Entry<String,Integer> room: rooms.entrySet()){
            Log.i(TAG,"Room: "+room.getKey()+" Value: "+room.getValue());
            if (maxEntry == null || room.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = room;
            }
        }
        return maxEntry.getKey();
    }
    private List<String> getNearbyDevices(){
        boolean scanSuccess=manager.startScan();
        if(!scanSuccess){
            Log.i(TAG,"Unable to scan.");
        }
        List<ScanResult> devices=manager.getScanResults();
        List <String> ssids=new ArrayList<String>();
        for(ScanResult device: devices){
            String ssid=device.SSID;
            Log.i(TAG, "Device: " + ssid + " RSSI: " + device.level);
            if(ssid.contains("ESP")){
                String mac=getMacFromSSID(ssid);
                ssids.add(ssid + " " +device.level+" MAC from ssid: "+mac+"Room from db lookup: "+deviceDBHelper.getRoomFromMac(mac));
            }
        }
        deviceDBHelper.dumpDBtoLog();
        return ssids;
    }

    public String getMacFromSSID(String ssid){
        int indexOf_=ssid.indexOf("_");
        return ssid.substring(indexOf_+1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.actionRefresh:
                adapter.clear();
                adapter.addAll(getNearbyDevices());
                adapter.notifyDataSetChanged();
                return true;
            case R.id.all:
                startActivity(new Intent(Home.this, AllDevices.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
