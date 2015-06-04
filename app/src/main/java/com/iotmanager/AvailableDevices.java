package com.iotmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import android.net.wifi.ScanResult;

import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



public class AvailableDevices extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private static final String NETWORK_PREFIX="ESP";
    private WifiManager manager;
    private ListView listView;
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

        listView=(ListView)findViewById(R.id.networkList);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                getDeviceSSIDs()
        );
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String selectedNetworkSSID=(String) listView.getItemAtPosition(position);
                final Network network=new Network(selectedNetworkSSID,getApplicationContext());

                if(network.isEnterprise()){
                    Toast.makeText(listView.getContext(), "No support for enterprise networks", Toast.LENGTH_LONG).show();
                    return;
                }
                else if(network.hasPassword()) {
                    setNetworkPassword(network,AvailableDevices.this);
                }

                final ProgressDialog progressDialog=new ProgressDialog(AvailableDevices.this);
                progressDialog.setMessage("Connecting ...");
                progressDialog.show();

                Thread connectThread=AndroidWifiHandler.connect(network,progressDialog, new Handler(){
                    //Handle what happens when thread has completed
                    @Override
                    public void handleMessage(Message msg){
                        Intent initialDeviceConfigurationIntent=new Intent(AvailableDevices.this,InitialDeviceConfiguration.class);
                        AvailableDevices.this.startActivity(initialDeviceConfigurationIntent);
                        progressDialog.dismiss();
                    }
                });
                connectThread.start();
            }
        });
    }

    public static void setNetworkPassword(final Network network,Context context){
        final EditText passwordInput=new EditText(context);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage("Enter password for network")
                .setView(passwordInput)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        network.setPassword(passwordInput.getText().toString());
                        dialog.cancel();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
        builder.show();
    }

    private List<String> getDeviceSSIDs(){
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
        return ssids;
    }
}
