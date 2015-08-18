package com.iotmanager;
import static com.iotmanager.Constants.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 *
 */
public class InitialDeviceConfiguration extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private static final String DEVICE_NAME_PLACEHOLDER="Connect Device";
    private EditText nameDevice;
    private EditText roomDevice;
    private Button nameDeviceSubmit;
    private Spinner deviceType;
    private DeviceCommunicationHandler deviceCommunicationHandler;
    private DeviceDBHelper deviceDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_device_configuration);
        setTitle("Initial Configuration");
        setUpSpinner();
        deviceCommunicationHandler=new DeviceCommunicationHandler(DEFAULT_DEVICE_IP,DEFAULT_DEVICE_TCP_PORT,this);
        deviceDBHelper=new DeviceDBHelper(this);
        nameDevice=(EditText)findViewById(R.id.nameDevice);
        roomDevice=(EditText)findViewById(R.id.roomDevice);
        nameDeviceSubmit=(Button)findViewById(R.id.nameDeviceSubmit);
        nameDeviceSubmit.setTextColor(Color.parseColor("#cccccc"));

        nameDeviceSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle cases of incomplete fields
                String name=nameDevice.getText().toString();
                String type=deviceType.getSelectedItem().toString();
                String room=roomDevice.getText().toString();
                if(name.equals("")){
                    Toast.makeText(InitialDeviceConfiguration.this,"Please enter a name for the device",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(type.equals("Type")){
                    Toast.makeText(InitialDeviceConfiguration.this,"Please select type of device",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(room.equals("")){
                    Toast.makeText(InitialDeviceConfiguration.this,"Please enter a room for the device",Toast.LENGTH_SHORT).show();
                    return;
                }
                nameDeviceSubmit.setTextColor(Color.BLACK); //Change the color to black on submit so all three fields are black when a configuration is submitted
                String nameResponse=deviceCommunicationHandler.sendDataGetResponse(COMMAND_NAME+nameDevice.getText().toString());
                String typeResponse=deviceCommunicationHandler.sendDataGetResponse(COMMAND_TYPE+deviceType.getSelectedItem().toString());
                String roomResponse=deviceCommunicationHandler.sendDataGetResponse(COMMAND_ROOM+roomDevice.getText().toString());
                String macResponse=deviceCommunicationHandler.sendDataGetResponse(COMMAND_MAC_GET); //Need the mac for the locator processing (match this mac address with the SSID which has the mac in it)
                if(nameResponse==null||typeResponse==null||roomResponse==null||macResponse==null){
                    Log.i(TAG, "Null response when sending: "+nameDevice.getText().toString()+", "+deviceType.getSelectedItem().toString()+", "+roomDevice.getText().toString());
                    Toast.makeText(InitialDeviceConfiguration.this,"Error writing to device",Toast.LENGTH_SHORT).show();
                    resetFields();
                    return;
                }
                //If device has been successfully configured move to the next page
                if(nameResponse.equals(RESPONSE_NAME_SUCCESS)&&typeResponse.equals(RESPONSE_TYPE_SUCCESS)&&roomResponse.equals(RESPONSE_ROOM_SUCCESS)){
                    Intent availableNetworksIntent= new Intent(InitialDeviceConfiguration.this,AvailableNetworks.class);
                    //Only add to database if it has been successfully configured on the firmware side
                    Device device=new Device(name,null,macResponse,room,type); //no ip yet (still default)
                    if(deviceDBHelper.addDevice(device)==-1){
                        Toast.makeText(InitialDeviceConfiguration.this,"That device configuration already exists",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    deviceDBHelper.dumpDBtoLog();
                    availableNetworksIntent.putExtra("Name",nameDevice.getText().toString());
                    startActivity(availableNetworksIntent);
                }
                else if(nameResponse.equals(RESPONSE_FAIL)||typeResponse.equals(RESPONSE_FAIL)||roomResponse.equals(RESPONSE_FAIL)){
                    //Error writing on device
                    //"Failed"
                    Toast.makeText(InitialDeviceConfiguration.this,"Failed to write to device",Toast.LENGTH_SHORT).show();
                    resetFields();
                }
            }
        });
    }

    /**
     * Resets text field to empty, the type Spinner to the placeholder item "Type" and the submit button back to the color #cccccc
     */
    private void resetFields(){
        deviceType.setSelection(0); //default selection in the spinner is the Type (a place holder)
        nameDevice.clearComposingText();
        nameDeviceSubmit.setTextColor(Color.parseColor("#cccccc"));
    }

    private void setUpSpinner(){
        deviceType=(Spinner)findViewById(R.id.deviceType);
        ArrayList<String> types=new ArrayList<String>();
        types.add("Type");
        types.add("Temperature");
        types.add("Lighting");
        types.add("Camera");
        types.add("Heater");
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                this,
                R.layout.spinner_layout,
                types
        );
        deviceType.setAdapter(adapter);
        deviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "Item selected");
                String selected=((TextView)parent.getChildAt(0)).getText().toString();
                //As long as the selected item in the spinner is not the placeholder item "Type"
                //set its color to black, indicating a valid color has been selected
                if(!selected.equals("Type")){
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
                else{
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#cccccc"));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_initial_device_configuration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.skip_initial_config:
                Intent availableNetworksIntent=new Intent(InitialDeviceConfiguration.this,AvailableNetworks.class);
                availableNetworksIntent.putExtra("Name",DEVICE_NAME_PLACEHOLDER);
                startActivity(availableNetworksIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
