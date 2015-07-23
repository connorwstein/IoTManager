package com.iotmanager;

import static com.iotmanager.Constants.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TemperatureConfiguration extends GenericConfiguration{
    private static final String TAG="Connors Debug";
    private CircleView circleTemperature;
    private CircleView circleHumidity;
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
        temperature=(TextView)findViewById(R.id.temperature);
        humidity=(TextView)findViewById(R.id.humidity);
        circleTemperature=(CircleView)findViewById(R.id.temperatureCircle);
        circleTemperature.setCircleRadius(220);
        circleTemperature.invalidate();
        circleHumidity=(CircleView)findViewById(R.id.humidityCircle);
        circleHumidity.setCircleRadius(220);
        circleHumidity.setCircleColor("#33CCFF");
        circleHumidity.invalidate();
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
        temperature.setText("Temperature: "+currentTemperature+"°C");
    }

}
