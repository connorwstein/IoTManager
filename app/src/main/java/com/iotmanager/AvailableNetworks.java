package com.iotmanager;
import static com.iotmanager.Constants.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AvailableNetworks extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private String selectedDevice;
    private String networkPassword="";
    private ListView networkListView;
    private WifiManager manager;
    private String espNetworkName;
    private String espNetworkPass;
    private DeviceCommunicationHandler deviceCommunicationHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        selectedDevice=getIntent().getStringExtra("Name");
        setTitle(selectedDevice);
        espNetworkName=getIntent().getStringExtra("espNetworkName");
        espNetworkPass=getIntent().getStringExtra("espNetworkPass");
        deviceCommunicationHandler=new DeviceCommunicationHandler(DEFAULT_DEVICE_IP,DEFAULT_DEVICE_TCP_PORT,this);
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
        networkListView=(ListView)findViewById(R.id.listNetworks);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                this,
                R.layout.list,
                getAllSSIDs()
        );
        networkListView.setAdapter(arrayAdapter);
        networkListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedNetworkSSID = (String) networkListView.getItemAtPosition(position);
                final Network network = new Network(selectedNetworkSSID, getApplicationContext());
                if (network.isEnterprise()) {
                    Toast.makeText(getApplicationContext(), "No support for enterprise networks", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (network.hasPassword()) {
                    setNetworkPasswordThenSend(network, AvailableNetworks.this);
                }
                else{
                    deviceCommunicationHandler.sendDataNoResponse(COMMAND_CONNECT+network.ssid+";"+network.password);
                    Toast.makeText(AvailableNetworks.this,"Sent connect request",Toast.LENGTH_SHORT).show();
                    Intent mainActivityIntent=new Intent(AvailableNetworks.this,Home.class);
                    startActivity(mainActivityIntent);
                }

            }
        });
    }

   private void setNetworkPasswordThenSend(final Network network,Context context){
       final EditText passwordInput=new EditText(context);
       passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
       AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage("Enter password for network")
                .setView(passwordInput)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(passwordInput.getText().toString().equals("")){
                            Toast.makeText(AvailableNetworks.this,"Please enter a password",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        network.setPassword(passwordInput.getText().toString());
                        deviceCommunicationHandler.sendDataNoResponse(COMMAND_CONNECT + network.ssid + ";" + network.password);
                        Toast.makeText(AvailableNetworks.this,"Send connect request",Toast.LENGTH_SHORT).show();
                        Intent mainActivityIntent=new Intent(AvailableNetworks.this,Home.class);
                        startActivity(mainActivityIntent);
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

    private List<String> getAllSSIDs(){
        List<ScanResult> networks=manager.getScanResults();
        List <String> ssids=new ArrayList<String>();
        for(int i=0;i<networks.size();i++){
            ssids.add(networks.get(i).SSID);

        }
        return ssids;
    }

}
