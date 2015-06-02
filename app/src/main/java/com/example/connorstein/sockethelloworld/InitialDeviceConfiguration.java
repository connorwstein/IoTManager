package com.example.connorstein.sockethelloworld;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.PrintWriter;
import java.net.Socket;


public class InitialDeviceConfiguration extends AppCompatActivity {
    private EditText nameDevice;
    private Button nameDeviceSubmit;
    private static final String TAG="sure2015test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_device_configuration);
        //Name:
        setTitle("Initial Configuration");
        nameDevice=(EditText)findViewById(R.id.nameDevice);
        nameDeviceSubmit=(Button)findViewById(R.id.nameDeviceSubmit);
        nameDeviceSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog p = new ProgressDialog(InitialDeviceConfiguration.this);
                p.setMessage("Sending name..");
                p.show();
                Log.i(TAG, "Clicked name button");
                new Thread(new Runnable() {
                    public void run() {
                        Socket s = null;
                        PrintWriter out;
                        try {
                            s = new Socket("192.168.4.1", 80);
                            out = new PrintWriter(s.getOutputStream());
                            out.write("Name:" + nameDevice.getText().toString());
                            out.flush();
                        } catch (Exception e) {
                            Log.i(TAG, "Exception " + e.getMessage());
                        }
                        p.dismiss();
                        Intent startAvailableNetworks = new Intent(InitialDeviceConfiguration.this,AvailableNetworks.class);
                        startAvailableNetworks.putExtra("Name",nameDevice.getText().toString());
                        startActivity(startAvailableNetworks);
                    }

                }).start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_initial_device_configuration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }
}
