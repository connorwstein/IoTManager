package com.iotmanager;

import static com.iotmanager.Constants.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.iotmanager.Constants.DEFAULT_DEVICE_TCP_PORT;

public class LightingConfiguration extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private TextView ipAddress;
    private TextView macAddress;
    private TextView lightStatus;
    private String ip;
    private String mac;
    private String name;
    private String currentLightStatus;
    private Button lightingOnOff;
    private DeviceCommunicationHandler deviceCommunicationHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "On create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_configuration);
        getDeviceInformation();
        initViews();
        deviceCommunicationHandler=new DeviceCommunicationHandler(ip,DEFAULT_DEVICE_TCP_PORT,this);
        getLightStatus();
        lightStatus.setText(currentLightStatus);
        lightingOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLightStatus();
            }
        });
    }
    private void getDeviceInformation(){
        Intent deviceInformation=getIntent();
        name=deviceInformation.getStringExtra("NAME");
        ip=deviceInformation.getStringExtra("IP");
        mac=deviceInformation.getStringExtra("MAC");
    }

    private void initViews(){
        setTitle(name);
        ipAddress=(TextView)findViewById(R.id.lightingIpAddress);
        macAddress=(TextView)findViewById(R.id.lightingMacAddress);
        lightStatus=(TextView)findViewById(R.id.lightStatus);
        lightingOnOff=(Button)findViewById(R.id.lightingOnOff);
        ipAddress.setText(ip);
        macAddress.setText(mac);
    }

    @Override
    protected void onResume(){
        Log.i(TAG, "On resume");
        super.onResume();
    }

    private void getLightStatus(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_LIGHTING_GET);
        if(response!=null){
            currentLightStatus=response;
            lightStatus.setText(currentLightStatus);
        }
        else{
            currentLightStatus="Not Available";
            lightStatus.setText(currentLightStatus);
        }
    }
    private void updateLightStatus(){
        Log.i(TAG,"Update light status");
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_LIGHTING_SET);
        getLightStatus();
    }

    private void convertToAccessPoint(){
        Log.i(TAG,"Convert to AP");
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_RUN_AP);
        Intent returnToMain = new Intent(LightingConfiguration.this, Home.class);
        startActivity(returnToMain);
    }

    private void renameDevice(){
        Log.i(TAG,"Device Rename");
        final EditText rename=new EditText(LightingConfiguration.this);
        rename.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(LightingConfiguration.this)
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

    private void sendRename(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_NAME+name);
        if(response.equals(RESPONSE_NAME_SUCCESS)){
            Toast.makeText(this,"Device renamed",Toast.LENGTH_SHORT).show();
            setTitle(name);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lighting_configuration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()){
            case R.id.renameDevice:
                renameDevice();
                break;
            case R.id.accessPoint:
                convertToAccessPoint();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
