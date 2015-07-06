package com.iotmanager;

import static com.iotmanager.Constants.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TemperatureConfiguration extends GenericConfiguration{
    private static final String TAG="Connors Debug";
    private TextView ipAddress;
    private TextView macAddress;
    private String currentTemperature;
    private String currentHumidity;
    private TextView temperature;
    private TextView humidity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_configuration);
        getDeviceInformation();
        initViews();
        deviceCommunicationHandler=new DeviceCommunicationHandler(ip,DEFAULT_DEVICE_TCP_PORT,this);
        getTemperatureAndHumidity();
    }
    private void initViews(){
        setTitle(name);
        ipAddress=(TextView)findViewById(R.id.temperatureIpAddress);
        macAddress=(TextView)findViewById(R.id.temperatureMacAddress);
        ipAddress.setText(ip);
        macAddress.setText(mac);
        temperature=(TextView)findViewById(R.id.temperature);
        humidity=(TextView)findViewById(R.id.humidity);
    }

    private void getTemperatureAndHumidity(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_TEMPERATURE_GET);
        Log.i(TAG,"Response from device: "+response);
        if(response!=null){
            String[] tempAndHum=response.split("\\s");
            currentHumidity=tempAndHum[0].substring(0,tempAndHum[0].length()-1)+"."+tempAndHum[0].substring(tempAndHum[0].length()-1);
            currentTemperature=tempAndHum[1].substring(0,tempAndHum[1].length()-1)+"."+tempAndHum[1].substring(tempAndHum[1].length()-1);
        }
        else{
            currentTemperature="Not Available";
            currentHumidity="Not Available";
        }
        humidity.setText("Humidity: "+currentHumidity +"%");
        temperature.setText("Temperature: "+currentTemperature);
    }

}
