package com.example.connorstein.IoTManager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="sure2015test";
    private GridView devicesGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        devicesGridView=(GridView)findViewById(R.id.gridView);
        devicesGridView.setAdapter(new ImageAdapter(this));

        devicesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent startDeviceCategory = new Intent(MainActivity.this, DeviceCategory.class);
            Log.i(TAG, "Position "+ position +" id "+id);
            startDeviceCategory.putExtra("Position",""+position);
            startActivity(startDeviceCategory);
            }
        });

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
