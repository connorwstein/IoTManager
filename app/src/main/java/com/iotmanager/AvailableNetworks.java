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


/**
 * Displays a list of the available networks that the ESP device can be connected to
 */
public class AvailableNetworks extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private String selectedDevice;
    private ListView networkListView;
    private WifiManager manager;
    private DeviceCommunicationHandler deviceCommunicationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        selectedDevice=getIntent().getStringExtra("Name");
        setTitle(selectedDevice);
        deviceCommunicationHandler=new DeviceCommunicationHandler(DEFAULT_DEVICE_IP,DEFAULT_DEVICE_TCP_PORT,this);
        String newIP=getIntent().getStringExtra("New IP");
        if(newIP!=null){
            deviceCommunicationHandler.setIP(newIP); //In case the user changes networks from an existing network
            //i.e. not from AP mode
        }
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

    /**
     * List all the available networks
     * Selecting one sends a command to the ESP device telling it to connect to that selected network and then returns to the main activity
     * (even if the device does not actually connect)
     * There is no way to verify whether the device has successfully connected, because if it does connect then it will not longer be in access point mode
     * and thus the android device is unable to talk to it. In theory you could wait until the ESP_XXX ssid is no longer visible but that is
     * unreliable (it may try to connect, stop broadcasting, fail to connect and then go back into AP mode), and scanning repeatedly for SSIDs is time consuming.
     * Also, since the password of this network is only known by the user, if they sent the wrong password, they have to send it again via the Add button from the MainActivity
     * Current solution is to just send the command, then the user can connect to the same network (outside of the app, if it does not autoconnect) and then broadcast
     * for the device on the network by clicking on the device category (Temperature, Lights etc.).
     */
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
                    deviceCommunicationHandler.sendDataNoResponse(COMMAND_CONNECT+network.ssid+";");
                    Toast.makeText(AvailableNetworks.this,"Sent connect request, ensure android is connected to the same network",Toast.LENGTH_LONG).show();
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
                        Toast.makeText(AvailableNetworks.this,"Sent connect request",Toast.LENGTH_SHORT).show();
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
