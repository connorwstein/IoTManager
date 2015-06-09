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

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.net.Socket;

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
    private DeviceCommunicationHandler deviceCommunicationHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "On create");
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
        deviceCommunicationHandler=new DeviceCommunicationHandler(ip,DEFAULT_DEVICE_TCP_PORT,this);
        String response=deviceCommunicationHandler.sendDataGetResponse("Lighting Get");
        if(response!=null){
            currentLightStatus=response;
        }
        else{
            currentLightStatus="Not Available";
        }
        lightStatus.setText(currentLightStatus);
        lightingOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLightStatus();
            }
        });
    }

    @Override
    protected void onResume(){
        Log.i(TAG,"On resume");
        super.onResume();
    }
    private void updateLightStatus(){
        Log.i(TAG,"Update light status");
        deviceCommunicationHandler.sendDataNoResponse("Lighting Set");
        String response=deviceCommunicationHandler.sendDataGetResponse("Lighting Get");
        if(response!=null){
            currentLightStatus=response;
            lightStatus.setText(currentLightStatus);
        }
        else{
            currentLightStatus="Not Available";
            lightStatus.setText(currentLightStatus);
        }
    }


    private void convertToAccessPoint(){
        Log.i(TAG,"Convert to AP");
//        final ProgressDialog progressDialog=new ProgressDialog(LightingConfiguration.this);
//        progressDialog.setMessage("Converting to AP..");
//        progressDialog.show();
//        Thread sendAP=SocketClient.tcpSend("Run AP", ip, DEFAULT_DEVICE_TCP_PORT, progressDialog, new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                progressDialog.dismiss();
//                handlePostSend(msg);
//                Intent returnToMain = new Intent(LightingConfiguration.this, MainActivity.class);
//                startActivity(returnToMain);
//            }
//        });
//        sendAP.start();
        deviceCommunicationHandler.sendDataNoResponse("Run AP");
        Intent returnToMain = new Intent(LightingConfiguration.this, MainActivity.class);
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
//        final ProgressDialog progressDialog=new ProgressDialog(LightingConfiguration.this);
//        progressDialog.setMessage("Updating device name..");
//        progressDialog.show();
//        Thread sendRename=SocketClient.tcpSend("Name:" + name, ip, DEFAULT_DEVICE_TCP_PORT, progressDialog, new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                progressDialog.dismiss();
//                handlePostSend(msg);
//                setTitle(name);
//            }
//        });
//        sendRename.start();
        String response=deviceCommunicationHandler.sendDataGetResponse("Name:"+name);
        if(response!=null){
            Toast.makeText(this,"Device renamed",Toast.LENGTH_SHORT).show();
        }
    }

//    private void handlePostSend(Message msg){
//        switch(msg.getData().getInt("Error code")){
//            case 0:
//                Toast.makeText(LightingConfiguration.this, "Error sending data. Ensure the device is still available on the network.", Toast.LENGTH_SHORT).show();
//                break;
//            case 1:
//                //Toast.makeText(LightingConfiguration.this,"Succesfully updated light",Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }


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
