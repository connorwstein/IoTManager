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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class TemperatureConfiguration extends AppCompatActivity{
    private static final String TAG="Connors Debug";
    private static final int TCP_PORT=80;
    private TextView ipAddress;
    private TextView macAddress;
    private String ip;
    private String mac;
    private String currentTemperature;
    private SeekBar temperatureSlider;
    private TextView temperature;
    private String name;
    private DeviceCommunicationHandler deviceCommunicationHandler;
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
    private void getDeviceInformation(){
        Intent deviceInformation=getIntent();
        name=deviceInformation.getStringExtra("NAME");
        ip=deviceInformation.getStringExtra("IP");
        mac=deviceInformation.getStringExtra("MAC");
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

    private void renameDevice(){
        final EditText rename=new EditText(TemperatureConfiguration.this);
        rename.setInputType(InputType.TYPE_CLASS_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(TemperatureConfiguration.this)
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
        if (response.equals(RESPONSE_NAME_SUCCESS)) {
            Toast.makeText(this,"Device renamed",Toast.LENGTH_SHORT).show();
            setTitle(name);
        }
    }

    private void convertToAccessPoint(){
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_RUN_AP);
        Intent returnToMain = new Intent(TemperatureConfiguration.this, Home.class);
        startActivity(returnToMain);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_temperature_configuration, menu);
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
