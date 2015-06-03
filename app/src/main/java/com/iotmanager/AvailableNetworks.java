package com.iotmanager;
import static com.iotmanager.Constants.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AvailableNetworks extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private String selectedDevice;
    private String networkPassword="";
    private ListView networkListView;
    private WifiManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        selectedDevice=getIntent().getStringExtra("Name");
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
        networkListView=(ListView)findViewById(R.id.listNetworks);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                ssids
        );
        networkListView.setAdapter(arrayAdapter);
        networkListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedNetworkSSID = (String) networkListView.getItemAtPosition(position);
                final Network network = new Network(selectedNetworkSSID, getApplicationContext());

                Log.i(TAG, "clicked item: " + network.ssid);
                if (network.isEnterprise()) {
                    Log.i(TAG, "Enterprise network selected");
                    Toast.makeText(getApplicationContext(), "No support for enterprise networks", Toast.LENGTH_LONG).show();
                    return;
                }
                if (network.hasPassword()) {
                    final EditText passwordInput = new EditText(AvailableNetworks.this);
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    AlertDialog.Builder builder = new AlertDialog.Builder(AvailableNetworks.this)
                            .setMessage("Enter password for network")
                            .setView(passwordInput)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    network.setPassword(passwordInput.getText().toString());
//                                    ConnectDeviceToRouter tellDeviceToConnect=new ConnectDeviceToRouter();
//                                    tellDeviceToConnect.execute(network,getApplicationContext(),progressDialog);
                                    Thread connectDeviceThread=SocketClient.tcpSend("Connect:"+network.ssid+";"+network.password,DEFAULT_DEVICE_IP,DEFAULT_DEVICE_PORT);
                                    connectDeviceThread.start();
                                    try{
                                        connectDeviceThread.join();
                                    }
                                    catch(InterruptedException e){
                                        Log.i(TAG,"Interrupted exception");
                                    }
                                    Intent mainActivityIntent=new Intent(AvailableNetworks.this,MainActivity.class);
                                    startActivity(mainActivityIntent);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.cancel();
                                }
                            });
                    builder.show();
                } else {
//                    ConnectDeviceToRouter tellDeviceToConnect=new ConnectDeviceToRouter();
//                    tellDeviceToConnect.execute(network,getApplicationContext(),progressDialog);
                    tellDeviceConnect(network);
                }

            }
        });
    }

    private Thread tellDeviceConnect(Network network){
        final String ssid=network.ssid;
        final String password=network.password;
        Thread connectDeviceThread=new Thread(new Runnable(){
            Socket s = null;
            PrintWriter out;
            @Override
            public void run() {
                try {
                    s = new Socket(DEFAULT_DEVICE_IP, DEFAULT_DEVICE_PORT);
                    out = new PrintWriter(s.getOutputStream());
                    out.write("Connect:" +ssid+";"+password);
                    out.flush();
                } catch (Exception e) {
                    Log.i(TAG, "Exception When Telling Device To Connect" + e.getMessage());
                }
            }
        });
        return connectDeviceThread;
    }

}
