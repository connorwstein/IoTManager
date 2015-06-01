package com.example.connorstein.sockethelloworld;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="sure2015test";
    private static final String SAVED_DEVICES_FILE="ESP_DEVICES";
    private static final int FILE_READ_BUF_SIZE=1024;
    private ListView devicesListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        devicesListView=(ListView)findViewById(R.id.devices);
//        ArrayList<String>devices=new ArrayList<String>();
//        devices.add("Hello world");
//        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
//                this,
//                android.R.layout.simple_list_item_1,
//                devices
//        );

        broadcastForDevices();
//        devicesListView.setAdapter(arrayAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.add_device:
                Intent intent=new Intent(MainActivity.this,AddDevice.class);
                startActivity(intent);
                return true;
            case R.id.broadcast_for_device_ips:
                broadcastForDevices();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void broadcastForDevices(){
        GetIpViaUdpBroadcast getDevicesInfo= new GetIpViaUdpBroadcast();
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Broadcasting for device info ...");
        progressDialog.show();
        getDevicesInfo.execute(this,progressDialog,devicesListView);

    }
}
