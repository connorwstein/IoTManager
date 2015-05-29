package com.example.connorstein.sockethelloworld;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class AddDevice extends AppCompatActivity {
    private WifiManager manager;
    private ListView listView;
    private static final String TAG="sure2015test";
    private static final String NETWORK_PREFIX="ESP";
    private boolean connected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.default_page_name);
        manager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanForNetworks();
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
                scanForNetworks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void scanForNetworks(){
        boolean scanSuccess=manager.startScan();
        if(!scanSuccess){
            Log.i(TAG,"Unable to scan.");
        }
        List<ScanResult> networks=manager.getScanResults();
        List <String> ssids=new ArrayList<String>();
        for(int i=0;i<networks.size();i++){
            if(networks.get(i).SSID.contains(NETWORK_PREFIX)) {
                ssids.add(networks.get(i).SSID);
            }
        }
        listView=(ListView)findViewById(R.id.networkList);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                ssids
        );
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "clicked item");
                String desiredNetwork=(String) listView.getItemAtPosition(position);

                connected=connect(desiredNetwork);
                if(!connected){
                    Log.i(TAG,"Not connected");
                    return;
                }
                WifiInfo wifiInfo=manager.getConnectionInfo();
                while(!wifiInfo.getSSID().equals("\""+desiredNetwork+"\"")){
                    wifiInfo=manager.getConnectionInfo();
                    Log.i(TAG,"Current SSID: "+wifiInfo.getSSID()+"Desired SSID: "+desiredNetwork);

                    try{
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e){
                        Log.i(TAG,"Interrupted exception");
                    }
                }
                Intent newActivity = new Intent(AddDevice.this, Device.class);
                newActivity.putExtra("Device", (String) listView.getItemAtPosition(position));
                startActivity(newActivity);
            }
        });
    }

    public boolean connect(String device){
        final WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + device + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        if(manager.addNetwork(conf)==-1){
            Log.i(TAG,"Add network fail");
            return false;
        }
        List<WifiConfiguration> configs = manager.getConfiguredNetworks();
        for (WifiConfiguration i : configs) {
            if (i.SSID != null && i.SSID.equals("\"" + device + "\"")) {
                manager.disconnect();
                if(manager.enableNetwork(i.networkId, true)==false){
                    Log.i(TAG,"Enable Network fail ");
                    return false;
                }
                if(manager.reconnect()==false) {
                    Log.i(TAG, "Reconnect fail");
                    return false;
                }
            }
        }
        return true;
    }
}
