package com.iotmanager;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.iotmanager.DevicesDBContract.DevicesDB;

public class Home extends AppCompatActivity {
    private static final String TAG="Connors Debug";

   // private GridView deviceCategoryGrid;
    private GridView devicesGridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        DeviceDBHelper db=new DeviceDBHelper(this);
        db.emptyDB(); //clear out for testing
        devicesGridView=(GridView)findViewById(R.id.deviceCategoryGrid);
        UdpBroadcast deviceBroadcast=new UdpBroadcast();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Broadcasting for devices");
        progressDialog.setCancelable(false);
        progressDialog.show();
        deviceBroadcast.execute(this, progressDialog,devicesGridView,getResources());//will block until devices have been found
    }

    @Override
    protected void onStart() {
        super.onStart();
        devicesGridView.setAdapter(null);
        UdpBroadcast deviceBroadcast=new UdpBroadcast();
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Broadcasting for devices");
        progressDialog.setCancelable(false);
        progressDialog.show();
        deviceBroadcast.execute(this, progressDialog, devicesGridView, getResources());//will block until devices have been found

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
            case R.id.lucky:
                Intent nearbyDevicesIntent= new Intent(this, NearbyDevices.class);
                startActivity(nearbyDevicesIntent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
