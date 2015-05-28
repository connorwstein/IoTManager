package com.example.connorstein.sockethelloworld;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
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


public class MainActivity extends AppCompatActivity {
    private WifiManager manager;
    private ListView listView;
    private static final String TAG="sure2015test";
    private static final String NETWORK_PREFIX="ESP";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.default_page_name);
//        String host="74.125.226.120";
//        int port=80;
//        String data="GET / HTTP/1.1\r\n\r\n";
//        SendDataViaSocket req=new SendDataViaSocket(host,port,data);
        manager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanForNetworks();
        udpClient test=new udpClient();
        test.execute();
        //req.execute();

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
                Intent newActivity = new Intent(MainActivity.this, Device.class);
                newActivity.putExtra("Device", (String) listView.getItemAtPosition(position));
                startActivity(newActivity);
            }
        });
    }
}
