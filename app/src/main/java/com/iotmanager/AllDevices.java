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

/**
 * Displays all devices on the network (same network as the phone)
 */
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
//    Causes the broadcast to be called when user comes back to this activity from a different activity, a bit annoying for the user, but does ensure that clicking on the icons will actually open their configuration (you know the devices are on the network)
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        broadcast(HELLO_DEVICES);
//    }

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
    private void broadcast(String broadcastMessage){
        devicesGridView.setAdapter(null);//empty the adapter so that no devices are visible when broadcasting
        final ProgressDialog pd=ProgressDialog.show(AllDevices.this, null,"Broadcasting...",false);
        //Handler determines what should happen after the UDP broadcast has been sent out
        //Note that a callback is necessary so that we can have the progress dialog running during the broadcast
        Handler broadcastCallback=new Handler() {
            @Override
            public void handleMessage(Message msg) {
                pd.dismiss();
                handlePostBroadcast((ArrayList<Device>)msg.getData().getSerializable("Devices")); //To pass an object around, make it serializable
            }
        };
        DeviceCommunicationHandler.broadcastForDevices(broadcastMessage, broadcastCallback); //Start the broadcast
    }
    public void handlePostBroadcast(final ArrayList<Device> devices){
        //Broadcasting will return a list of Device objects
        if (devices.size()==0) {
            Toast.makeText(AllDevices.this, "No devices on this network. Try broadcasting again and ensure the correct password was sent to device when connecting it to the network.", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Device device : devices) {
            device.log(); //Print device detected to the log
            if (deviceDBHelper.getID(device) == -1) {
                //If the device does not already exist in the database, add it
                Log.i(TAG,"Adding device to db");
                deviceDBHelper.addDevice(device);
            }
        }
        adapter = new DeviceThumbnailAdapter(AllDevices.this, getResources(), devices); //will display all the device thumbnails
        devicesGridView.setAdapter(adapter);
        devicesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Depending on which type of device is clicked, load a different configuration activity
                Intent i = ResponseParser.createIntentForDeviceConfiguration(devices.get(position), AllDevices.this);
                if (i != null)
                    AllDevices.this.startActivity(i);
                else
                    Log.i(TAG, "Error with device information");
            }
        });
    }

}
