package com.iotmanager;
import static com.iotmanager.Constants.*;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class InitialDeviceConfiguration extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private EditText nameDevice;
    private Button nameDeviceSubmit;
    private Spinner deviceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_device_configuration);
        //Name:
        setTitle("Initial Configuration");
        deviceType=(Spinner)findViewById(R.id.deviceType);
        ArrayList<String> types=new ArrayList<String>();
        types.add("Temperature");
        types.add("Lighting");

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                this,
                R.layout.support_simple_spinner_dropdown_item,
                types
        );
        deviceType.setAdapter(adapter);

        nameDevice=(EditText)findViewById(R.id.nameDevice);
        nameDeviceSubmit=(Button)findViewById(R.id.nameDeviceSubmit);
        nameDeviceSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog p = new ProgressDialog(InitialDeviceConfiguration.this);
                p.setMessage("Sending name..");
                p.show();
                Log.i(TAG, "Clicked name button");
                Thread sendConfigurationInformation=SocketClient.tcpSend("Name:"+nameDevice.getText().toString(),DEFAULT_DEVICE_IP,DEFAULT_DEVICE_PORT);
                sendConfigurationInformation.start();
                new Thread(new Runnable() {
                    public void run() {
                        Socket s = null;
                        PrintWriter out;
                        try {
                            s = new Socket(DEFAULT_DEVICE_IP, DEFAULT_DEVICE_PORT);
                            out = new PrintWriter(s.getOutputStream());
                            out.write("Name:" + nameDevice.getText().toString());
                            out.flush();
                            out.write("Type:" + deviceType.getSelectedItem().toString());
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
