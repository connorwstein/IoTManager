package com.iotmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
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

import com.iotmanager.R;

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
    private static final String LIGHT_OFF="OFF";
    private static final String LIGHT_ON="ON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_configuration);
        currentLightStatus=LIGHT_OFF;
        Intent deviceInformation=getIntent();
        name=deviceInformation.getStringExtra("NAME");
        setTitle(name);
        ip=deviceInformation.getStringExtra("IP");
        mac=deviceInformation.getStringExtra("MAC");
        ipAddress=(TextView)findViewById(R.id.lightingIpAddress);
        macAddress=(TextView)findViewById(R.id.lightingMacAddress);
        lightStatus=(TextView)findViewById(R.id.lightStatus);
        lightingOnOff=(Button)findViewById(R.id.lightingOnOff);
        ipAddress.setText(ip);
        macAddress.setText(mac);
        sendLightGetRequest();
        lightingOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLightStatus();
            }
        });
    }

    private void updateLightStatus(){
        final ProgressDialog progressDialog=new ProgressDialog(LightingConfiguration.this);
        progressDialog.setMessage("Updating device light status..");
        progressDialog.show();
        String invertLightStatus;
        if(currentLightStatus.equals(LIGHT_OFF)){
            invertLightStatus=LIGHT_ON;
        }
        else{
            invertLightStatus=LIGHT_OFF;
        }
        Thread sendLightValue=SocketClient.tcpSend("Light Set:"+invertLightStatus,ip,DEFAULT_DEVICE_TCP_PORT,progressDialog,new Handler(){
            @Override
            public void handleMessage(Message msg){
                progressDialog.dismiss();
                handlePostSend(msg);
            }
        });
        sendLightValue.start();
    }

    private void recevieCurrentLightStatus(final ProgressDialog progressDialog){
        Thread sendTemperatureValue=SocketClient.tcpReceive(progressDialog,new Handler(){
            @Override
            public void handleMessage(Message msg){
                progressDialog.dismiss();
                currentLightStatus=msg.getData().getString("Received");
                Log.i(TAG, "Received " + currentLightStatus);
                lightStatus.setText(currentLightStatus);
            }
        });
        sendTemperatureValue.start();
    }
    private void sendLightGetRequest(){
        final ProgressDialog progressDialog=new ProgressDialog(LightingConfiguration.this);
        progressDialog.setMessage("Getting device status..");
        progressDialog.show();
        Thread sendTemperatureValue=SocketClient.tcpSend("Light Get",ip,DEFAULT_DEVICE_TCP_PORT,progressDialog,new Handler(){
            @Override
            public void handleMessage(Message msg){
                handlePostSend(msg);
                recevieCurrentLightStatus(progressDialog);
            }
        });
        sendTemperatureValue.start();
    }

    private void renameDevice(){
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
        final ProgressDialog progressDialog=new ProgressDialog(LightingConfiguration.this);
        progressDialog.setMessage("Updating device name..");
        progressDialog.show();
        Thread sendRename=SocketClient.tcpSend("Rename:" + name, ip, DEFAULT_DEVICE_TCP_PORT, progressDialog, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                handlePostSend(msg);
                setTitle(name);
            }
        });
        sendRename.start();
    }

    private void handlePostSend(Message msg){
        switch(msg.getData().getInt("Error code")){
            case 0:
                Toast.makeText(LightingConfiguration.this, "Error sending data. Ensure the device is still available on the network.", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                //Toast.makeText(LightingConfiguration.this,"Succesfully updated light",Toast.LENGTH_SHORT).show();
                break;
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
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
