package com.iotmanager;

import static com.iotmanager.Constants.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.iotmanager.Constants.DEFAULT_DEVICE_TCP_PORT;

public class LightingConfiguration extends GenericConfiguration {
    private static final String TAG="Connors Debug";
    private TextView lightStatus;
    private String currentLightStatus;
    private Button lightingOnOff;

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

    private void initViews(){
        setTitle(name);
        lightStatus=(TextView)findViewById(R.id.lightStatus);
        lightingOnOff=(Button)findViewById(R.id.lightingOnOff);
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

}
