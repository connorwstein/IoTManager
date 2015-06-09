package com.iotmanager;
import static com.iotmanager.Constants.*;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class InitialDeviceConfiguration extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private static final String DEVICE_NAME_PLACEHOLDER="Connect Device";
    private EditText nameDevice;
    private Button nameDeviceSubmit;
    private Spinner deviceType;
    private String espNetworkName;
    private String espNetworkPass;
    private DeviceCommunicationHandler deviceCommunicationHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_device_configuration);
        setTitle("Initial Configuration");
        setUpSpinner();
        espNetworkName=getIntent().getStringExtra("espNetworkName");
        espNetworkPass=getIntent().getStringExtra("espNetworkPass");
        deviceCommunicationHandler=new DeviceCommunicationHandler(DEFAULT_DEVICE_IP,DEFAULT_DEVICE_TCP_PORT,this);
        nameDevice=(EditText)findViewById(R.id.nameDevice);
        nameDeviceSubmit=(Button)findViewById(R.id.nameDeviceSubmit);
        nameDeviceSubmit.setTextColor(Color.parseColor("#cccccc"));
        nameDeviceSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nameDevice.getText().toString().equals("")){
                    Toast.makeText(InitialDeviceConfiguration.this,"Please enter a name for the device",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(deviceType.getSelectedItem().toString().equals("Type")){
                    Toast.makeText(InitialDeviceConfiguration.this,"Please select type of device",Toast.LENGTH_SHORT).show();
                    return;
                }
                nameDeviceSubmit.setTextColor(Color.BLACK);
                String nameResponse=deviceCommunicationHandler.sendDataGetResponse(COMMAND_NAME+nameDevice.getText().toString());
                String typeResponse=deviceCommunicationHandler.sendDataGetResponse(COMMAND_TYPE+deviceType.getSelectedItem().toString());
                if(nameResponse==null||typeResponse==null){
                    Log.i(TAG, "Null response");
                    Toast.makeText(InitialDeviceConfiguration.this,"Error writing to device",Toast.LENGTH_SHORT).show();
                    resetFields();
                    return;
                }
                if(nameResponse.equals(RESPONSE_NAME_SUCCESS)&&typeResponse.equals(RESPONSE_TYPE_SUCCESS)){
                    Intent availableNetworksIntent= new Intent(InitialDeviceConfiguration.this,AvailableNetworks.class);
                    availableNetworksIntent.putExtra("Name",nameDevice.getText().toString());
                    availableNetworksIntent.putExtra("espNetworkName",espNetworkName);
                    availableNetworksIntent.putExtra("espNetworkPass",espNetworkPass);
                    startActivity(availableNetworksIntent);
                }
                else if(nameResponse.equals(RESPONSE_FAIL)||typeResponse.equals(RESPONSE_FAIL)){
                    //Error writing on device
                    //"Failed"
                    Toast.makeText(InitialDeviceConfiguration.this,"Failed to write to device",Toast.LENGTH_SHORT).show();
                    resetFields();
                }
            }
        });
    }
    private void resetFields(){
        deviceType.setSelection(0);
        nameDevice.clearComposingText();
        nameDeviceSubmit.setTextColor(Color.parseColor("#cccccc"));
    }

    private void setUpSpinner(){
        deviceType=(Spinner)findViewById(R.id.deviceType);
        ArrayList<String> types=new ArrayList<String>();
        types.add("Type");
        types.add("Temperature");
        types.add("Lighting");
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                this,
                R.layout.spinner_layout,
                types
        );
        deviceType.setAdapter(adapter);
        deviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "Item selected");
                String selected=((TextView)parent.getChildAt(0)).getText().toString();
                if(!selected.equals("Type")){
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
                else{
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#cccccc"));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
        switch(item.getItemId()){
            case R.id.skip_initial_config:
                Intent availableNetworksIntent=new Intent(InitialDeviceConfiguration.this,AvailableNetworks.class);
                availableNetworksIntent.putExtra("Name",DEVICE_NAME_PLACEHOLDER);
                startActivity(availableNetworksIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
