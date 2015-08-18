package com.iotmanager;

import static com.iotmanager.Constants.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import static com.iotmanager.Constants.DEFAULT_DEVICE_TCP_PORT;

/**
 * Dimmer class, only works with the dimmer hardware
 */
public class LightingConfiguration extends GenericConfiguration {
    private static final String TAG="Connors Debug";
    private int currentLightStatus;
    private SeekBar lightDimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "On create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_configuration);
        getDeviceInformation();
        deviceCommunicationHandler=new DeviceCommunicationHandler(device.getIp(),DEFAULT_DEVICE_TCP_PORT,this);
        initViews();
        getLightStatus();
        updateTitle();

        lightDimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG,"Dimmer set: "+progress);
                deviceCommunicationHandler.sendDataNoResponse(COMMAND_LIGHTING_SET + progress);
                currentLightStatus=progress;
                updateTitle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG,"Start tracking");

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG,"Stop tracking");

            }
        });
    }

    private void initViews(){
        lightDimmer=(SeekBar)findViewById(R.id.lightingDimmer);
    }
    private void updateTitle(){
        if(currentLightStatus==100){
            setTitle(device.getName()+": "+"On");
        }
        else if(currentLightStatus==0){
            setTitle(device.getName()+": "+"Off");
        }
        else if(currentLightStatus==-1){
            setTitle(device.getName()+": "+"Not Available");
        }
        else{
            setTitle(device.getName()+": "+currentLightStatus+"%");
        }
    }
    @Override
    protected void onResume(){
        Log.i(TAG, "On resume");
        super.onResume();
    }

    private void getLightStatus(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_LIGHTING_GET);
        //respond with a number 0 == OFF 100== ON
        if(response!=null){
            try{
                currentLightStatus=Integer.parseInt(response);
                lightDimmer.setProgress(currentLightStatus);
            }
            catch(NumberFormatException e){
                Log.i(TAG,"Numer format exception - device did not send a number as a light value");
                currentLightStatus=-1;
            }
        }
        else{
            currentLightStatus=-1;
        }
    }


}
