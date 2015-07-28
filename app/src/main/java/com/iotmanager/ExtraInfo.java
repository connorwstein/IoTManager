package com.iotmanager;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ExtraInfo extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private TextView ip;
    private TextView mac;
    private Device device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_info);
        Intent i=this.getIntent();
        this.device=(Device)i.getSerializableExtra("Device");
        Log.i(TAG, "Extra info");
        setTitle(this.device.getName());
        ip=(TextView)findViewById(R.id.ipAddress);
        mac=(TextView)findViewById(R.id.macAddress);
        ip.setText(this.device.getIp());
        mac.setText(this.device.getMac());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_extra_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
