package com.iotmanager;

import static com.iotmanager.Constants.*;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TemperatureConfiguration extends GenericConfiguration{
    private static final String TAG="Connors Debug";
    private String currentTemperature;
    private String currentHumidity;
    private TextView temperature;
    private TextView temperatureTimeStamp;
    private TextView humidityTimeStamp;
    private TextView humidity;
    private ImageView temperatureCircle;
    private ImageView temperatureCircleBorder;
    private ImageView humidityCircle;
    private ImageView humidityCircleBorder;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(menu.findItem(10)==null){
            menu.add(0, 10, 0, "Refresh Reading"); //arbitrary id of 10, just used to check if item already exists in menu
            //solves bug where menu item keeps getting added
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == 10) {
            getTemperatureAndHumidity();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_configuration);
        getDeviceInformation();
        initViews();
        deviceCommunicationHandler=new DeviceCommunicationHandler(device.getIp(),DEFAULT_DEVICE_TCP_PORT,this);
        getTemperatureAndHumidity();
    }
    private void initViews(){
        setTitle(device.getName());
        temperature=(TextView)findViewById(R.id.temperature);
        humidity=(TextView)findViewById(R.id.humidity);
        temperatureTimeStamp=(TextView)findViewById(R.id.temperatureTimeStamp);
        humidityTimeStamp=(TextView)findViewById(R.id.humidityTimeStamp);
        temperatureCircle=(ImageView)findViewById(R.id.temperatureCircle);
        ((GradientDrawable)temperatureCircle.getBackground()).setColor(Color.parseColor("#FF6666"));


        temperatureCircleBorder=(ImageView)findViewById(R.id.temperatureCircleBorder);
        ((GradientDrawable)temperatureCircleBorder.getBackground()).setColor(Color.parseColor("#FFFFFF"));

        humidityCircleBorder=(ImageView)findViewById(R.id.humidityCircleBorder);
        ((GradientDrawable)humidityCircleBorder.getBackground()).setColor(Color.parseColor("#FFFFFF"));

        humidityCircle=(ImageView)findViewById(R.id.humidityCircle);
        ((GradientDrawable)humidityCircle.getBackground()).setColor(Color.parseColor("#33CCFF"));


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
            humidity.setText("Not Available");
            temperature.setText("Not Available");
            return;
        }
        humidity.setText("Humidity: "+currentHumidity +"%");
        temperature.setText("Temperature: "+currentTemperature+"°C");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String timestamp=dateFormat.format(Calendar.getInstance().getTime());
        humidityTimeStamp.setText(timestamp);
        temperatureTimeStamp.setText(timestamp);
    }

}
