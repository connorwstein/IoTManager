package com.example.connorstein.sockethelloworld;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="sure2015test";
    private static final String SAVED_DEVICES_FILE="ESP_DEVICES";
    private static final int FILE_READ_BUF_SIZE=1024;
    private GridView devicesGridView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        devicesGridView=(GridView)findViewById(R.id.gridView);
        devicesGridView.setAdapter(new ImageAdapter(this));
//        ImageView lights=(ImageView)devicesGridView.getItemAtPosition(0);
//        ImageView temperature=(ImageView)devicesGridView.getItemAtPosition(1);

        devicesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent startDeviceCategory = new Intent(MainActivity.this, DeviceCategory.class);
                Log.i(TAG, "Position "+ position +" id "+id);
                startDeviceCategory.putExtra("Position",""+position);
                 startActivity(startDeviceCategory);
            }
        });

//        devicesListView=(ListView)findViewById(R.id.devices);
//        broadcastForDevices();
//        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent deviceConfigurationIntent=new Intent(MainActivity.this,DeviceConfiguration.class);
//                deviceConfigurationIntent.putExtra("Device", devicesListView.getItemAtPosition(position).toString());
//                startActivity(deviceConfigurationIntent);
//
//            }
//        });
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
                Intent intent=new Intent(this,AvailableDevices.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
