package com.iotmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class DeviceConfiguration extends AppCompatActivity {

    private static final String TAG="Connors Debug";
    private TextView ipAddress;
    private TextView macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_configuration);
        Intent deviceInformation=getIntent();
        setTitle(deviceInformation.getStringExtra("NAME"));//display only the device IP for now
        ipAddress=(TextView)findViewById(R.id.ipAddress);
        macAddress=(TextView)findViewById(R.id.macAddress);
        ipAddress.setText(deviceInformation.getStringExtra("IP"));
        macAddress.setText(deviceInformation.getStringExtra("MAC"));

        //TO DO be able to configure more detailed settings here
        //i.e. open socket with ip and send data
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_configuration, menu);
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
