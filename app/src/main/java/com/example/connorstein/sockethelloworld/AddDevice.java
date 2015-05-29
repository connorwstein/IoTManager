package com.example.connorstein.sockethelloworld;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
        setContentView(R.layout.activity_add_device);
        setTitle(R.string.default_page_name);
        manager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanForNetworks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.actionRefresh:
                Log.i(TAG,"Clicked refresh");
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
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Toast.makeText(listView.getContext(), "Connecting ...", Toast.LENGTH_LONG).show();

//                Log.i(TAG, "clicked item");
                final String desiredNetwork=(String) listView.getItemAtPosition(position);
//                Log.i(TAG, "Pre toast");
//                Log.i(TAG, "Post toast");
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        connect(desiredNetwork, null, getApplicationContext(), manager); //this method blocks until connected

                    }
                });
//                if(!connected){
//                    Log.i(TAG,"Not connected");
//                    return;
//                }
                Intent newActivity = new Intent(AddDevice.this, Device.class);
                newActivity.putExtra("Device", (String) listView.getItemAtPosition(position));
                startActivity(newActivity);
            }
        });
    }


    public static boolean connect(String device,String password,Context context,WifiManager manager){
        final WifiConfiguration conf = new WifiConfiguration();

        conf.SSID = "\"" + device + "\"";
        if(password!=null){
            conf.preSharedKey = "\""+ password +"\"";
        }
        else{
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

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

        WifiInfo info=manager.getConnectionInfo();
        while((info.getIpAddress())==0){
            //Wait until non-zero IP address (once a non-zero ip address is obtained, you are connected to the network)
            //Tried to use NetworkInfo.DetailedState to check if it was CONNECTED
            //However the api method said it remained in OBTAINING_IPADDR state even after it obtained an ip (must be bug)
            info=manager.getConnectionInfo();
            try{
                Thread.sleep(100);
            }
            catch(InterruptedException e){
                Log.i(TAG,"Interrupted exception");
            }
        }
        return true;
    }




}
