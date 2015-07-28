package com.iotmanager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;


public class AllDevices extends AppCompatActivity {

    private static final String TAG="Connors Debug";
    private GridView devicesGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_devices);
        setTitle("All Devices");
        devicesGridView=(GridView)findViewById(R.id.deviceCategoryGrid);
        Broadcast.broadcastForDevices(this, devicesGridView, getResources(), null); //will block until gridview filled

    }

    @Override
    protected void onStart() {
        super.onStart();
        devicesGridView.setAdapter(null);
        Broadcast.broadcastForDevices(this, devicesGridView, getResources(), null); //will block until gridview filled
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_devices, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.actionRefresh:
                Broadcast.broadcastForDevices(this, devicesGridView, getResources(), null); //will block until gridview filled
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
