package com.iotmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import static com.iotmanager.Constants.*;


public class AllDevices extends AppCompatActivity {

    private static final String TAG="Connors Debug";
    private GridView devicesGridView;
    private DeviceThumbnailAdapter adapter;
    private DeviceDBHelper deviceDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_devices);
        setTitle("All Devices");
        deviceDBHelper=new DeviceDBHelper(AllDevices.this);
        devicesGridView=(GridView)findViewById(R.id.deviceCategoryGrid);
        broadcast(HELLO_DEVICES);
    }

    public void handlePostBroadcast(final ArrayList<Device> devices){
        if (devices.size()==0) {
            Toast.makeText(AllDevices.this, "No devices on this network. Try broadcasting again and ensure the correct password was sent to device when connecting it to the network.", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Device device : devices) {
            device.log();
            if (deviceDBHelper.getID(device) == -1) {
                //if the device does not already exist in the database, add it
                Log.i(TAG,"Adding device to db");
                deviceDBHelper.addDevice(device);
            }
        }
        adapter = new DeviceThumbnailAdapter(AllDevices.this, getResources(), devices);
        devicesGridView.setAdapter(adapter);
        devicesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = ResponseParser.createIntentForDeviceConfiguration(devices.get(position), AllDevices.this);
                if (i != null)
                    AllDevices.this.startActivity(i);
                else
                    Log.i(TAG, "Error with device information");
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        broadcast(HELLO_DEVICES);
    }

    private void broadcast(String broadcastMessage){
        devicesGridView.setAdapter(null);//empty the adapter so that no devices are visible when broadcast
        final ProgressDialog pd=ProgressDialog.show(AllDevices.this, null,"Broadcasting...",false);
        Handler broadcastCallback=new Handler() {
            @Override
            public void handleMessage(Message msg) {
                pd.dismiss();
                handlePostBroadcast((ArrayList<Device>)msg.getData().getSerializable("Devices"));
            }
        };
        DeviceCommunicationHandler.broadcastForDevices(broadcastMessage, broadcastCallback);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_devices, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.actionRefresh:
                broadcast(HELLO_DEVICES);
                return true;
            case R.id.add:
                startActivity(new Intent(AllDevices.this,AvailableDevices.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
