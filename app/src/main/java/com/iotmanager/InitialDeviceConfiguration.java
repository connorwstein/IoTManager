package com.iotmanager;
import static com.iotmanager.Constants.*;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class InitialDeviceConfiguration extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private static final String DEVICE_NAME_PLACEHOLDER="Connect Device";
    private EditText nameDevice;
    private Button nameDeviceSubmit;
    private Spinner deviceType;
    private String espNetworkName;
    private String espNetworkPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_device_configuration);
        //Name:
        setTitle("Initial Configuration");
        setUpSpinner();
        espNetworkName=getIntent().getStringExtra("espNetworkName");
        espNetworkPass=getIntent().getStringExtra("espNetworkPass");

        nameDevice=(EditText)findViewById(R.id.nameDevice);
        nameDeviceSubmit=(Button)findViewById(R.id.nameDeviceSubmit);
        nameDeviceSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(InitialDeviceConfiguration.this);
                progressDialog.setMessage("Sending name..");
                progressDialog.show();
                if(nameDevice.getText().toString().equals("")){
                    progressDialog.dismiss();
                    Toast.makeText(InitialDeviceConfiguration.this,"Please enter a name for the device",Toast.LENGTH_SHORT).show();
                    return;
                }
                Thread sendConfigurationInformation=SocketClient.tcpSend("Name:"+nameDevice.getText().toString(), DEFAULT_DEVICE_IP,DEFAULT_DEVICE_PORT, progressDialog,
                        new Handler(){
                            @Override
                            public void handleMessage(Message msg){
                                progressDialog.dismiss();
                                handlePostSend(msg);
                            }
                        });
                sendConfigurationInformation.start();
            }
        });
    }

    private void handlePostSend(Message msg){
        if(msg.getData().getInt("Error code")==0){
            Toast.makeText(InitialDeviceConfiguration.this,"Error sending data, verify connection to device",Toast.LENGTH_LONG).show();
        }
        else{
            Intent availableNetworksIntent= new Intent(InitialDeviceConfiguration.this,AvailableNetworks.class);
            availableNetworksIntent.putExtra("Name",nameDevice.getText().toString());
            availableNetworksIntent.putExtra("espNetworkName",espNetworkName);
            availableNetworksIntent.putExtra("espNetworkPass",espNetworkPass);
            startActivity(availableNetworksIntent);
        }
    }

    private void setUpSpinner(){
        deviceType=(Spinner)findViewById(R.id.deviceType);
        ArrayList<String> types=new ArrayList<String>();
        types.add("Temperature");
        types.add("Lighting");
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                this,
                R.layout.support_simple_spinner_dropdown_item,
                types
        );
        deviceType.setAdapter(adapter);
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
