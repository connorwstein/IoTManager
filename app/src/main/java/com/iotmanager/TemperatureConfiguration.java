package com.iotmanager;

import static com.iotmanager.Constants.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

public class TemperatureConfiguration extends GenericConfiguration{
    private static final String TAG="Connors Debug";
    private TextView ipAddress;
    private TextView macAddress;
    private String currentTemperature;
    private SeekBar temperatureSlider;
    private TextView temperature;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_configuration);
        getDeviceInformation();
        initViews();
        deviceCommunicationHandler=new DeviceCommunicationHandler(ip,DEFAULT_DEVICE_TCP_PORT,this);
        getTemperature();
        temperatureSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                temperature.setText(Integer.toString(progress));
                progressValue = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "start tracking touch");
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateTemperature(progressValue);
            }
        });
    }
    private void initViews(){
        setTitle(name);
        ipAddress=(TextView)findViewById(R.id.temperatureIpAddress);
        macAddress=(TextView)findViewById(R.id.temperatureMacAddress);
        ipAddress.setText(ip);
        macAddress.setText(mac);
        temperatureSlider=(SeekBar)findViewById(R.id.temperatureSlider);
        temperature=(TextView)findViewById(R.id.temperature);
    }

    private void getTemperature(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_TEMPERATURE_GET);
        if(response!=null){
            currentTemperature=response;
            temperature.setText(currentTemperature);
            temperatureSlider.setProgress(Integer.parseInt(currentTemperature));
        }
        else{
            currentTemperature="Not Available";
            temperature.setText(currentTemperature);
            temperatureSlider.setProgress(0);
        }
    }

    private void updateTemperature(int progressValue){
        Log.i(TAG, "stop tracking touch");
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_TEMPERATURE_SET+progressValue);
        getTemperature();
    }

}
