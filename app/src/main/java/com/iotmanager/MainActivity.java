package com.iotmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="Connors Debug";

    private GridView deviceCategoryGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        deviceCategoryGrid=(GridView)findViewById(R.id.deviceCategoryGrid);
        deviceCategoryGrid.setAdapter(new ImageAdapter(this,getResources()));

        deviceCategoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent deviceCategoryIntent = new Intent(MainActivity.this, DeviceCategory.class);
                //Use position to indicate category
                //Position 0: Lighting, Position 1: Temperature
                deviceCategoryIntent.putExtra("Position",Integer.toString(position));
                startActivity(deviceCategoryIntent);
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
            case R.id.availableDevices:
                Intent availableDevicesIntent=new Intent(this,AvailableDevices.class);
                startActivity(availableDevicesIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
