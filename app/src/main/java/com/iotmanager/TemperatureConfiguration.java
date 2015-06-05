package com.iotmanager;
import static com.iotmanager.Constants.*;

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
    private String ip;
    private String mac;
    private String currentTempterature;
    private SeekBar temperatureSlider;
    private TextView temperature;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_configuration);
        Intent deviceInformation=getIntent();
        name=deviceInformation.getStringExtra("NAME");
        setTitle(name);
        ip=deviceInformation.getStringExtra("IP");
        mac=deviceInformation.getStringExtra("MAC");
        ipAddress=(TextView)findViewById(R.id.temperatureIpAddress);
        macAddress=(TextView)findViewById(R.id.temperatureMacAddress);
        ipAddress.setText(ip);
        macAddress.setText(mac);
        temperatureSlider=(SeekBar)findViewById(R.id.temperatureSlider);
        temperature=(TextView)findViewById(R.id.temperature);
        sendTemperatureGetRequest();
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
    private void sendTemperatureGetRequest(){
        final ProgressDialog progressDialog=new ProgressDialog(TemperatureConfiguration.this);
        progressDialog.setMessage("Getting device temperature..");
        progressDialog.show();
        Thread sendTemperatureValue=SocketClient.tcpSend("Temperature Get",ip,DEFAULT_DEVICE_TCP_PORT,progressDialog,new Handler(){
            @Override
            public void handleMessage(Message msg){
                handlePostSend(msg);
                recevieCurrentTemperature(progressDialog);
            }
        });
        sendTemperatureValue.start();
    }

    private void recevieCurrentTemperature(final ProgressDialog progressDialog){
        Thread sendTemperatureValue=SocketClient.tcpReceive(progressDialog, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                currentTempterature = msg.getData().getString("Received");
                Log.i(TAG, "Received " + currentTempterature);
                temperature.setText(currentTempterature);
                temperatureSlider.setProgress(Integer.parseInt(currentTempterature));
            }
        });
        sendTemperatureValue.start();
    }

    private void updateTemperature(int progressValue){
        Log.i(TAG, "stop tracking touch");
        final ProgressDialog progressDialog=new ProgressDialog(TemperatureConfiguration.this);
        progressDialog.setMessage("Updating device temperature..");
        progressDialog.show();
        Thread sendTemperatureValue=SocketClient.tcpSend("Temperature Set:" + Integer.toString(progressValue), ip, DEFAULT_DEVICE_TCP_PORT, progressDialog, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                handlePostSend(msg);
            }
        });
        sendTemperatureValue.start();
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
        final ProgressDialog progressDialog=new ProgressDialog(TemperatureConfiguration.this);
        progressDialog.setMessage("Updating device name..");
        progressDialog.show();
        Thread sendRename=SocketClient.tcpSend("Rename:" + name, ip, DEFAULT_DEVICE_TCP_PORT, progressDialog, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                handlePostSend(msg);
                setTitle(name);
            }
        });
        sendRename.start();
    }

    private void handlePostSend(Message msg){
        switch(msg.getData().getInt("Error code")){
            case 0:
                Toast.makeText(TemperatureConfiguration.this,"Error sending data. Ensure the device is still available on the network.", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                //Toast.makeText(TemperatureConfiguration.this,"Succesfully updated!",Toast.LENGTH_SHORT).show();
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
            case R.id.renameDevice:
                renameDevice();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
