package com.iotmanager;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static com.iotmanager.Constants.*;

//NOTE assume that the phone can always see the router and all devices are connected to the router

public class Home extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private WifiManager manager;
    private DeviceDBHelper deviceDBHelper;
    private GridView nearbyDevices;
    private DeviceThumbnailAdapter adapter;
    private String currentRoom;
    private static final int RSSI_THRESHOLD=-55;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        deviceDBHelper=new DeviceDBHelper(Home.this);
        //deviceDBHelper.emptyDB();
        nearbyDevices=(GridView)findViewById(R.id.nearbyDevices);
        manager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        deviceDBHelper.dumpDBtoLog();
        Log.i(TAG, "Sending low power mode");
        getNearbyDevices();
    }

    //Broadcast again in the onstart method so that if the user click back it
    //will broadcast again
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Log.i(TAG, "Sending low power mode");
//        getNearbyDevices();
//    }
    private void getNearbyDevices(){
        broadcast(LOCATION_MODE);
    }


    private void broadcast(String broadcastMessage){
        nearbyDevices.setAdapter(null);//empty the adapter so that no devices are visible when broadcast

        if(broadcastMessage.equals(LOCATION_MODE)){
            final ProgressDialog pd=ProgressDialog.show(Home.this, null,"Setting devices in locator mode...",false);
            Handler locationModeCallback=new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    pd.dismiss();
                    currentRoom=getRoom();
                    if(currentRoom!=null){
                        setTitle("Current room: " +currentRoom);
                    }
                    else{
                        setTitle("Indeterminate Room");
                    }
                    broadcast(HELLO_DEVICES);
                }
            };

            DeviceCommunicationHandler.broadcastForDevices(broadcastMessage,locationModeCallback);
        }
        else{
            final ProgressDialog pd=ProgressDialog.show(Home.this, null,"Guessing your current room...",false);
            Handler broadcastCallback=new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    pd.dismiss();
                    handlePostBroadcast((ArrayList<Device>)msg.getData().getSerializable("Devices"));
                }
            };
            DeviceCommunicationHandler.broadcastForDevices(broadcastMessage, broadcastCallback);

        }
    }
    public void handlePostBroadcast(final ArrayList<Device> devices){

        if (devices.size()==0) {
            Toast.makeText(Home.this, "No devices in this room responded", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(currentRoom==null){
            Toast.makeText(Home.this,"Unable to determine room", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, "---------Start devices before filtering --------");
        for (Device device : devices) {
            device.log();
        }
        Log.i(TAG, "---------End devices before filtering --------");

        ResponseParser.filterByRoom(devices, currentRoom);
        for (Device device : devices) {
            Log.i(TAG,"Received device from broadcast: ");
            device.log();
            if (deviceDBHelper.getID(device) == -1) {
                //if the device does not already exist in the database, add it
                deviceDBHelper.addDevice(device);
            }
        }
        adapter = new DeviceThumbnailAdapter(Home.this, getResources(), devices);
        nearbyDevices.setAdapter(adapter);
        nearbyDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = ResponseParser.createIntentForDeviceConfiguration(devices.get(position), Home.this);
                if (i != null)
                    Home.this.startActivity(i);
                else
                    Log.i(TAG, "Error with device information");
            }
        });
    }
    private String getRoom(){
        boolean scanSuccess=manager.startScan();
        if(!scanSuccess){
            Log.i(TAG,"Unable to scan.");
        }
        Log.i(TAG,"------------Getting current room--------------");
        //When getting the current room, should first tell devices to lower their power via router
        HashMap<String,Integer> rooms= new HashMap();
        List<ScanResult> devices=manager.getScanResults();
        for(ScanResult device: devices){
            String ssid=device.SSID;
            if(ssid.contains("ESP")&&ssid.length()==21){ //length ensures that it is a locator device with ESP_MACADDRESS.. and not ESP_somethingelse
                String mac=getMacFromSSID(ssid);
                String room=deviceDBHelper.getRoomFromMac(mac);

                if(room==null){
                    Log.i(TAG,"Detected mac not in the db");
                    continue;
                }
                Log.i(TAG,"MAC Found: "+mac+" in room "+room+" RSSI: "+device.level);
                if(rooms.containsKey(room)&&device.level>RSSI_THRESHOLD){
                    rooms.put(room,rooms.get(room)+1);
                }
                else if(device.level>RSSI_THRESHOLD){
                    rooms.put(room,1);
                }
            }
        }
        if(rooms.size()==0){
            Log.i(TAG,"No locator devices detected");
            return null;
        }
        //deviceDBHelper.dumpDBtoLog();
        HashMap.Entry<String,Integer>maxEntry=null;
        //Note that if two rooms have the same amount of detected locators, then it will return the first room
        for(HashMap.Entry<String,Integer> room: rooms.entrySet()){
            Log.i(TAG,"Room: "+room.getKey()+" Value: "+room.getValue());
            if (maxEntry == null || room.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = room;
            }
        }
        Log.i(TAG,"------------Done getting current room --------------: "+maxEntry.getKey());
        return maxEntry.getKey();
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
                Log.i(TAG, "Sending low power mode");
                getNearbyDevices();
                return true;
            case R.id.all:
                startActivity(new Intent(Home.this, AllDevices.class));
                return true;
            case R.id.availableDevices:
                startActivity(new Intent(Home.this,AvailableDevices.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
