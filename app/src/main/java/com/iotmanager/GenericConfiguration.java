package com.iotmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import static com.iotmanager.Constants.COMMAND_NAME;
import static com.iotmanager.Constants.COMMAND_RUN_AP;
import static com.iotmanager.Constants.COMMAND_TYPE;
import static com.iotmanager.Constants.RESPONSE_NAME_SUCCESS;

/**
 * Created by connorstein on 15-06-09.
 */
public abstract class GenericConfiguration extends AppCompatActivity {

    public DeviceCommunicationHandler deviceCommunicationHandler;
    public String ip;
    public String mac;
    public String name;

    public void getDeviceInformation(){
        Intent deviceInformation=getIntent();
        name=deviceInformation.getStringExtra("NAME");
        ip=deviceInformation.getStringExtra("IP");
        mac=deviceInformation.getStringExtra("MAC");
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
                        name=rename.getText().toString();
                        sendRename();
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

    public void sendRename(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_NAME+name);
        if (response.equals(RESPONSE_NAME_SUCCESS)) {
            Toast.makeText(this, "Device renamed", Toast.LENGTH_SHORT).show();
            setTitle(name);
        }
    }

    public void convertToAccessPoint(){
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_RUN_AP);
        Intent returnToMain = new Intent(GenericConfiguration.this, Home.class);
        startActivity(returnToMain);
    }

    public void changeType(){
        final CharSequence[] items={"Temperature","Lighting"};
        AlertDialog.Builder builder = new AlertDialog.Builder(GenericConfiguration.this)
                .setTitle("Select New Type")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceCommunicationHandler.sendDataNoResponse(COMMAND_TYPE+items[which].toString());
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
            case R.id.accessPoint:
                convertToAccessPoint();
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
