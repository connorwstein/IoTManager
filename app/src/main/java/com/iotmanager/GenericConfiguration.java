package com.iotmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import static com.iotmanager.Constants.COMMAND_NAME;
import static com.iotmanager.Constants.COMMAND_RUN_AP;
import static com.iotmanager.Constants.COMMAND_TYPE;
import static com.iotmanager.Constants.RESPONSE_NAME_SUCCESS;
import static com.iotmanager.Constants.COMMAND_ROOM;
import static com.iotmanager.Constants.RESPONSE_ROOM_SUCCESS;


/**
 * Created by connorstein on 15-06-09.
 * Standard configuration tools for the device i.e. rename, connect to a different network, become and AP etc.
 */
public abstract class GenericConfiguration extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private static final CharSequence[] DEVICE_TYPES={"Temperature","Lighting","Camera","Heater"};
    public DeviceCommunicationHandler deviceCommunicationHandler;
    public Device device;
    private DeviceDBHelper deviceDBHelper=new DeviceDBHelper(this);

    public void getDeviceInformation(){
        Intent deviceInformation=getIntent();
        this.device=(Device)deviceInformation.getSerializableExtra("Device");
    }

    public void renameDevice(){
        final EditText rename=new EditText(GenericConfiguration.this);
        rename.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(GenericConfiguration.this)
                .setMessage("Enter new device name")
                .setView(rename)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName=rename.getText().toString();
                        sendRename(newName);
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

    public void sendRename(String newName){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_NAME+newName);
        if (response.equals(RESPONSE_NAME_SUCCESS)) {
            Toast.makeText(this, "Device renamed to "+newName, Toast.LENGTH_SHORT).show();
            //deviceDBHelper.dumpDBtoLog();
            Log.i(TAG,"Device "+this.device.getName()+" rename: "+newName);
            int id=deviceDBHelper.getID(device); //get old id;
            this.device.setName(newName); //modify device
            if(id==-1){
                //device is not in the database, but should be (can happen when flashing and clearing the db)
                //just add it with the new name
                Log.i(TAG,"Device not in db, adding ");
                this.device.log();
                deviceDBHelper.addDevice(this.device);
                deviceDBHelper.dumpDBtoLog();
            }
            else{
                Log.i(TAG,"Device in db, updating ");
                this.device.log();
                deviceDBHelper.updateDevice(id, device);
                deviceDBHelper.dumpDBtoLog();
            }
            setTitle(this.device.getName());
            returnToHomeAfterConfigChange();
        }
        else{
            Toast.makeText(this, "Device rename failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void changeRoom(){
        final EditText newRoom=new EditText(GenericConfiguration.this);
        newRoom.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(GenericConfiguration.this)
                .setMessage("Enter new room for device")
                .setView(newRoom)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newRoomText=newRoom.getText().toString();
                        sendNewRoom(newRoomText);
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
    public void sendNewRoom(String newRoom){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_ROOM+newRoom);
        if (response.equals(RESPONSE_ROOM_SUCCESS)) {
            Toast.makeText(this, "Device room changed to "+newRoom, Toast.LENGTH_SHORT).show();
            int id=deviceDBHelper.getID(this.device);
            this.device.setRoom(newRoom);
            if(id==-1){
                //device is not in the database, but should be (can happen when flashing and clearing the db
                deviceDBHelper.addDevice(this.device);
            }
            else{
                deviceDBHelper.updateDevice(id, this.device);
            }
            returnToHomeAfterConfigChange();
        }
        else{
            Toast.makeText(this, "Device room change failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void returnToHomeAfterConfigChange(){
        Intent backToHome=new Intent(GenericConfiguration.this,Home.class);
        startActivity(backToHome);
    }

    public void convertToAccessPoint(){
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_RUN_AP);
        Intent returnToMain = new Intent(GenericConfiguration.this, Home.class);
        startActivity(returnToMain);
    }

    public void changeType(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GenericConfiguration.this)
                .setTitle("Select New Type")
                .setItems(DEVICE_TYPES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newType=DEVICE_TYPES[which].toString();
                        deviceCommunicationHandler.sendDataNoResponse(COMMAND_TYPE + newType);
                        int id=deviceDBHelper.getID(device);
                        GenericConfiguration.this.device.setType(newType);
                        deviceDBHelper.updateDevice(id,GenericConfiguration.this.device);
                        Intent homeIntent=new Intent(GenericConfiguration.this,Home.class);
                        startActivity(homeIntent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    public void changeNetwork(){
        Intent changeNetworkIntent = new Intent(GenericConfiguration.this,AvailableNetworks.class);
        changeNetworkIntent.putExtra("New IP",this.device.getIp());
        startActivity(changeNetworkIntent);
    }

    public void showExtraInfo(){
        Intent extraInfoIntent = new Intent(GenericConfiguration.this,ExtraInfo.class);
        extraInfoIntent.putExtra("Device",this.device);
        startActivity(extraInfoIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.change_network:
                changeNetwork();
                break;
            case R.id.change_type:
                changeType();
                break;
            case R.id.renameDevice:
                renameDevice();
                break;
            case R.id.change_room:
                changeRoom();
                break;
            case R.id.accessPoint:
                convertToAccessPoint();
                break;
            case R.id.extraInfo:
                showExtraInfo();
                break;
            case R.id.locator_power_settings:
                sendPowerSettings();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    void sendPowerSettings(){
        final EditText power=new EditText(GenericConfiguration.this);
        power.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(GenericConfiguration.this)
                .setMessage("Enter power settings max[0-82];min[0-82] i.e. 40;0 ")
                .setView(power)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String power_setting = power.getText().toString();
                        String response = deviceCommunicationHandler.sendDataGetResponse("Power:" + power_setting);
                        if (response == null || response.equals("Power Set Fail")) {
                            Toast.makeText(GenericConfiguration.this, "Power set failed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else if (response.equals("Power Set")) {
                            Toast.makeText(GenericConfiguration.this, "Power set", Toast.LENGTH_SHORT).show();
                        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_generic_configuration, menu);
        return true;
    }
}
