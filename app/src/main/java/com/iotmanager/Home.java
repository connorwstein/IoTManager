package com.iotmanager;

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

    private GridView deviceCategoryGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        DeviceDBHelper dbHelper=new DeviceDBHelper(this);
        SQLiteDatabase myDb= dbHelper.getWritableDatabase();

        ContentValues test= new ContentValues();
        test.put(DevicesDB.COLUMN_ID, 1);
        test.put(DevicesDB.COLUMN_NAME, "TEMP");
        test.put(DevicesDB.COLUMN_ROOM,"Living");
        test.put(DevicesDB.COLUMN_TYPE,"Temperature");

        long rowid=myDb.insert(DevicesDB.TABLE_NAME, null, test);

        String[] projection={
                DevicesDB.COLUMN_ID,
                DevicesDB.COLUMN_NAME,
                DevicesDB.COLUMN_ROOM,
                DevicesDB.COLUMN_TYPE
        };
         Cursor c=myDb.query(DevicesDB.TABLE_NAME, projection, null,null,null,null,null);

//        c.moveToFirst();
//        Log.i(TAG,"DB helper test "+c.getString(c.getColumnIndex(DevicesDB.COLUMN_NAME_NAME)));

        deviceCategoryGrid=(GridView)findViewById(R.id.deviceCategoryGrid);
        deviceCategoryGrid.setAdapter(new ImageAdapter(this,getResources()));

        deviceCategoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent deviceCategoryIntent = new Intent(Home.this, DeviceCategory.class);
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
            case R.id.lucky:
                Intent nearbyDevicesIntent= new Intent(this, NearbyDevices.class);
                startActivity(nearbyDevicesIntent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
