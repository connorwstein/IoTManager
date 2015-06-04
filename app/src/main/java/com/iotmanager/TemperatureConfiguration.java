package com.iotmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iotmanager.R;

public class TemperatureConfiguration extends AppCompatActivity{
    private static final String TAG="Connors Debug";
    private static final int TCP_PORT=80;
    private TextView ipAddress;
    private TextView macAddress;
    private SeekBar temperatureSlider;
    private TextView temperature;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_configuration);
        Intent deviceInformation=getIntent();
        setTitle(deviceInformation.getStringExtra("NAME"));//display only the device IP for now
        final String ip=deviceInformation.getStringExtra("IP");
        ipAddress=(TextView)findViewById(R.id.temperatureIpAddress);
        macAddress=(TextView)findViewById(R.id.temperatureMacAddress);
        ipAddress.setText(deviceInformation.getStringExtra("IP"));
        macAddress.setText(deviceInformation.getStringExtra("MAC"));
        temperatureSlider=(SeekBar)findViewById(R.id.temperatureSlider);
        temperature=(TextView)findViewById(R.id.temperature);
        temperature.setText("0");
        temperatureSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                temperature.setText(Integer.toString(progress));
                progressValue=progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "start tracking touch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "stop tracking touch");
                final ProgressDialog p=new ProgressDialog(TemperatureConfiguration.this);
                p.setMessage("Updating device temperature..");
                p.show();
                Thread sendTemperatureValue=SocketClient.tcpSend("Temperature Set:"+Integer.toString(-progressValue),ip,TCP_PORT,p,new Handler(){
                    @Override
                    public void handleMessage(Message msg){
                        p.dismiss();
                        handlePostSend(msg);
                    }
                });
                sendTemperatureValue.start();
            }
        });
        //TO DO be able to configure more detailed settings here
        //i.e. open socket with ip and send data
    }

    private void handlePostSend(Message msg){
        switch(msg.getData().getInt("Error code")){
            case 0:
                Toast.makeText(TemperatureConfiguration.this,"Error sending data. Ensure the device is still available on the network.", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(TemperatureConfiguration.this,"Succesfully updated temperature",Toast.LENGTH_SHORT).show();
                break;
        }
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
