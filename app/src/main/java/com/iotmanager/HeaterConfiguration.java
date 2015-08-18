package com.iotmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.iotmanager.Constants.*;

/**
 * Heaters are represented as using physical lights in the demonstration (limited hardware) so all they have is an on off switch
 */
public class HeaterConfiguration extends GenericConfiguration {
    private Button heaterOnOff;
    private static final String DEVICE_ON="ON";
    private static final String DEVICE_OFF="OFF";
    private String status=DEVICE_OFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heater_configuration);
        heaterOnOff=(Button)findViewById(R.id.heaterOnOff);
        getDeviceInformation();
        setTitle(device.getName()+": "+status);
        deviceCommunicationHandler=new DeviceCommunicationHandler(device.getIp(),DEFAULT_DEVICE_TCP_PORT,this);
        heaterOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status.equals(DEVICE_OFF)){
                    deviceCommunicationHandler.sendDataNoResponse(COMMAND_HEATER_SET_ON);
                    status=DEVICE_ON;
                    setTitle(device.getName()+": "+status);
                }
                else{
                    deviceCommunicationHandler.sendDataNoResponse(COMMAND_HEATER_SET_OFF);
                    status=DEVICE_OFF;
                    setTitle(device.getName()+": "+status);
                }
            }
        });

    }


}
