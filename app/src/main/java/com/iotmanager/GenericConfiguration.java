package com.iotmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
    public DeviceCommunicationHandler deviceCommunicationHandler;
    public String ip;
    public String mac;
    public String name;
    public String type;
    public String room;
    private DeviceDBHelper deviceDBHelper=new DeviceDBHelper(this);

    public void getDeviceInformation(){
        Intent deviceInformation=getIntent();
        name=deviceInformation.getStringExtra("NAME");
        ip=deviceInformation.getStringExtra("IP");
        mac=deviceInformation.getStringExtra("MAC");
        type=deviceInformation.getStringExtra("TYPE");
        room=deviceInformation.getStringExtra("ROOM");
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
            deviceDBHelper.dumpDBtoLog();
            int id=deviceDBHelper.getIDSpecificDevice(name,room,type);
            if(id==-1){
                //device is not in the database, but should be (can happen when flashing and clearing the db)
                //just add it with the new name
                Log.i(TAG,"Device not in db, adding "+name+", "+room+", "+type);
                deviceDBHelper.addDevice(newName,room,type);
                deviceDBHelper.dumpDBtoLog();
            }
            else{
                Log.i(TAG,"Device in db, updating "+name+", "+room+", "+type);
                deviceDBHelper.updateDevice(id, newName, room, type);
                deviceDBHelper.dumpDBtoLog();
            }
            name=newName;
            setTitle(name);
            returnToDeviceCategoryAfterConfigChange();
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
            int id=deviceDBHelper.getIDSpecificDevice(name,room,type);
            if(id==-1){
                //device is not in the database, but should be (can happen when flashing and clearing the db
                deviceDBHelper.addDevice(name,newRoom,type);
            }
            else{
                deviceDBHelper.updateDevice(id, name, newRoom, type);
            }
            room=newRoom;
            returnToDeviceCategoryAfterConfigChange();
        }
        else{
            Toast.makeText(this, "Device room change failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void returnToDeviceCategoryAfterConfigChange(){
        Intent backToDeviceCategory=new Intent(GenericConfiguration.this,DeviceCategory.class);
        switch(type){
            case "Lighting":
                backToDeviceCategory.putExtra("Position","0");
                break;
            case "Temperature":
                backToDeviceCategory.putExtra("Position","1");
                break;
            case "Camera":
                backToDeviceCategory.putExtra("Position","2");
                break;
            default:
                Log.i(TAG,"Error changing room");
        }
        startActivity(backToDeviceCategory);
    }

    public void convertToAccessPoint(){
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_RUN_AP);
        Intent returnToMain = new Intent(GenericConfiguration.this, Home.class);
        startActivity(returnToMain);
    }

    public void changeType(){
        final CharSequence[] items={"Temperature","Lighting","Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(GenericConfiguration.this)
                .setTitle("Select New Type")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceCommunicationHandler.sendDataNoResponse(COMMAND_TYPE + items[which].toString());
                        deviceDBHelper.updateDevice(deviceDBHelper.getIDSpecificDevice(name,room,type),name,items[which].toString(),type);
                        type=items[which].toString();
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
        changeNetworkIntent.putExtra("New IP",ip);
        startActivity(changeNetworkIntent);
    }

    public void showExtraInfo(){
        Intent extraInfoIntent = new Intent(GenericConfiguration.this,ExtraInfo.class);
        extraInfoIntent.putExtra("IP",ip);
        extraInfoIntent.putExtra("MAC", mac);
        extraInfoIntent.putExtra("NAME",name);
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

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_generic_configuration, menu);
        return true;
    }
}
