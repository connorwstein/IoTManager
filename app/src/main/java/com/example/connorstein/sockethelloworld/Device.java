package com.example.connorstein.sockethelloworld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class Device extends AppCompatActivity {
    private static final String TAG="sure2015test";
    private String selectedDevice;
    private String networkPassword="";
    private ListView listNetworks;
    private static final String defaultIP="192.168.4.1";
    private static final int defaultPort=80;
    private WifiManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        selectedDevice=getIntent().getStringExtra("Device");
        setTitle(selectedDevice);
        Log.i(TAG, "Device: " + selectedDevice);
        manager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        listAllNetworks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.actionRefresh:
                listAllNetworks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void listAllNetworks(){
        boolean scanSuccess=manager.startScan();
        if(!scanSuccess){
            Log.i(TAG,"Unable to scan.");
        }
        List<ScanResult> networks=manager.getScanResults();
        List <String> ssids=new ArrayList<String>();
        for(int i=0;i<networks.size();i++){
            ssids.add(networks.get(i).SSID);

        }
        listNetworks=(ListView)findViewById(R.id.listNetworks);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                ssids
        );
        listNetworks.setAdapter(arrayAdapter);
        listNetworks.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String ssid=listNetworks.getItemAtPosition(position).toString();
//                Log.i(TAG, "clicked item: " + ssid);
//                if(isEnterprise(ssid)){
//                    Log.i(TAG,"Enterprise network selected");
//                    Toast.makeText(getApplicationContext(),"No support for enterprise networks", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                if(hasPassword(ssid)){
//                    //Toast.makeText(getApplicationContext(),"",Toast.LENGTH_LONG).show();
//                    AlertDialog.Builder builder=new AlertDialog.Builder(Device.this);
//                    builder.setTitle("Enter Password:");
//                    // Set up the input
//                    final EditText input = new EditText(Device.this);
//                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//                    builder.setView(input);
//                    // Set up the buttons
//                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            networkPassword = input.getText().toString();
//                            Log.i(TAG, "Password Inputed: " + networkPassword);
//                            Toast.makeText(getApplicationContext(),"Told device to connect", Toast.LENGTH_LONG).show();
//                            TcpClient req=new TcpClient(defaultIP,defaultPort,ssid+";"+networkPassword+"\r\n",getApplicationContext(),ssid,networkPassword,manager);
//                            req.execute();
//
//                        }
//                    });
//                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//                    builder.show();
//                }
//                else{
//                    TcpClient req=new TcpClient(defaultIP,defaultPort,ssid+";"+networkPassword+"\r\n",getApplicationContext(),ssid,networkPassword,manager);
//                    req.execute();
//                }


            }
        });
    }




}
