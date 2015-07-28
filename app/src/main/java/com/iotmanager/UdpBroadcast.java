package com.iotmanager;

import static com.iotmanager.Constants.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by connorstein on 15-05-28.
 */
public class UdpBroadcast extends AsyncTask<Object,Void,Boolean> {

    private static final int RECEIVE_BUFFER_SIZE=1024;
    private static final int MAX_NUM_RECEIVE_PACKETS=10;
    private static final int MAX_NUM_SEND_PACKETS=3;
    private static final int SOCKET_TIMEOUT = 100;
    private static final String TAG="Connors Debug";

    private String broadcastMessage;
    private ProgressDialog progressDialog;
    private Context context;
    private ArrayList<String> deviceResponses;
    private GridView devicesGridView;
    private Resources resources;
    private DeviceDBHelper deviceDBHelper;
    private String roomFilter;
    @Override
    protected Boolean doInBackground(Object... args) {
        context=(Context)args[0];
        progressDialog=(ProgressDialog)args[1];
        devicesGridView=(GridView)args[2];
        resources=(Resources) args[3];
        roomFilter=(String)args[4];
        broadcastMessage="Hello ESP Devices?";
        deviceResponses=new ArrayList<>();
        deviceDBHelper=new DeviceDBHelper(context);
        //Create UDP socket
        DatagramSocket udpBroadcastSocket=null;
        try{
            udpBroadcastSocket= new DatagramSocket();
            Log.i(TAG, "Created datagram socket");
        }
        catch(Exception e){
            Log.i(TAG,"Exception "+e.getMessage());
        }
        //sent MAX_NUM_SEND_PACKETS and assume responses will occur
        //to at least one
        //For each sent packet, attempt to receive MAX_NUM_RECEIVE_PACKETS
        //Gather all responses and remove duplicates if necessary
        int sentPackets=0;
        while(sentPackets<MAX_NUM_SEND_PACKETS) {
            try {
                byte sendBuffer[] = broadcastMessage.getBytes();
                sentPackets++;
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(DEFAULT_DEVICE_BROADCAST_IP), DEFAULT_DEVICE_UDP_PORT);
                Log.i(TAG, "Sending broadcast message");
                udpBroadcastSocket.setSoTimeout(SOCKET_TIMEOUT);
                udpBroadcastSocket.setBroadcast(true);
                udpBroadcastSocket.send(sendPacket);
                receiveMultiplePackets(udpBroadcastSocket);
            } catch (Exception e) {
                Log.i(TAG, "Exception has occured: " + e.getMessage());
            }
        }
        Log.i(TAG, "Number of devices that responded " + deviceResponses.size());
        if(deviceResponses.size()==0){
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean devicesDetected) {
        super.onPostExecute(devicesDetected);
        progressDialog.dismiss();
        if(!devicesDetected){
            Toast.makeText(context,"No devices on this network. Try broadcasting again and ensure the correct password was sent to device when connecting it to the network.",Toast.LENGTH_LONG).show();
            return;
        }
        final ArrayList<Device> devices=new ArrayList<>();
        for(String response:deviceResponses){
            devices.add(ResponseParser.createDeviceFromResponse(response));
        }

      ResponseParser.removeDuplicates(devices);
        for(Device device:devices){
            device.log();
            if(deviceDBHelper.getID(device)==-1){
                //if the device does not already exist in the database, add it
                deviceDBHelper.addDevice(device);
            }
        }
        if(roomFilter!=null){
            ResponseParser.filterByRoom(devices, roomFilter);
        }
        deviceDBHelper.dumpDBtoLog();
        devicesGridView.setAdapter(new ImageAdapter(context, resources, devices));
        devicesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i=ResponseParser.createIntentForDeviceConfiguration(devices.get(position),context);
                if(i!=null)
                    context.startActivity(i);
                else
                    Log.i(TAG,"Error with device information");
            }
        });
    }


    //Tries to receive MAX_NUM_RECEIVE_PACKETS and stores them
    //in member variable deviceResponses
    private void receiveMultiplePackets(DatagramSocket udpBroadcastSocket){
        int i;
        for(i=0;i<MAX_NUM_RECEIVE_PACKETS;i++){
            String responsePacketData=null;
            byte receiveBuffer[] = new byte[RECEIVE_BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try{
                udpBroadcastSocket.receive(receivePacket);
            }
            catch(IOException e){
                //Log.i(TAG,"IO exception receiving packet "+i);
                continue;
            }
           // Log.i(TAG, "Packet received, length: "+receivePacket.getLength());
            responsePacketData= new String(receivePacket.getData(), 0, receivePacket.getLength());
            Log.i(TAG, "Received: " + responsePacketData);
            if(responsePacketData.contains("IP:")&&responsePacketData.contains("MAC:")&&responsePacketData.contains("NAME:")
                    && responsePacketData.contains("ROOM:")&&responsePacketData.contains("TYPE:")){
                deviceResponses.add(responsePacketData);
            }
            else{
                Log.i(TAG,"Invalid Packet: "+responsePacketData);
            }

        }
    }
}
