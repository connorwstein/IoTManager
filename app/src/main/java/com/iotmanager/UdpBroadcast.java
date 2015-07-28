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
    private ListView devicesListView;
    private String deviceCategory;
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
        deviceResponses=new ArrayList<String>();

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
            //Stack the toasts so user has time to read it
            Toast.makeText(context,"No devices on this network. Try broadcasting again and ensure the correct password was sent to device when connecting it to the network.",Toast.LENGTH_LONG).show();
            return;
        }
        //List 0: Names, List 1: IPs, List all_devices: MAC adddresses list 3 rooms, list 4 types
        //Want to display only the names, and pass the rest to the device configuration activity
        //so if user clicks on a name more detailed information is available
        final ArrayList<ArrayList<String>>deviceInformation=ResponseParser.getDistinctDeviceInformation(deviceResponses);
        deviceDBHelper=new DeviceDBHelper(context);
        //Parallel index in the lists which represent each device
        for(int i=0;i<deviceInformation.get(0).size();i++){
            String name,room,type,mac;
            name=deviceInformation.get(0).get(i);
            mac=deviceInformation.get(2).get(i);
            room=deviceInformation.get(3).get(i);
            type=deviceInformation.get(4).get(i);
            Log.i(TAG, "Device found: " + name + ", " + deviceInformation.get(1).get(i) + ", " + deviceInformation.get(2).get(i) + ", " + room + ", " + type);
            if(deviceDBHelper.getIDSpecificDevice(name,room,type,mac)==-1) {
                //if the device does not already exist in the database, add it
                deviceDBHelper.addDevice(name, room, type,mac);
            }
        }
        if(roomFilter!=null){
            filterByRoom(deviceInformation,roomFilter);
        }
        deviceDBHelper.dumpDBtoLog();
        devicesGridView.setAdapter(new ImageAdapter(context, resources,deviceInformation.get(0),deviceInformation.get(4)));
        devicesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewGroup data=(ViewGroup)view;
                TextView text=(TextView)data.getChildAt(1);
                Log.i(TAG, text.getText().toString()+"Item clicked position: " + position + " id: " + id);
                Intent i=createIntentWithDeviceInformation(deviceInformation,position);
                if(i!=null)
                    context.startActivity(i);
                else
                    Log.i(TAG,"Error with device information");
            }
        });
    }

    private void filterByRoom(ArrayList<ArrayList<String>>deviceInformation, String room){

        for(int i=0;i<deviceInformation.get(0).size();i++){
            if(!deviceInformation.get(3).get(i).equals(room)){
                deviceInformation.get(0).remove(i);
                deviceInformation.get(1).remove(i);
                deviceInformation.get(2).remove(i);
                deviceInformation.get(3).remove(i);
                deviceInformation.get(4).remove(i);
            }
        }
    }
    private Intent createIntentWithDeviceInformation(final ArrayList<ArrayList<String>>deviceInformation, int position){
        Log.i(TAG, "Create Device info intent");
        Intent i=null;
        switch(deviceInformation.get(4).get(position)){
            case "Temperature":
                i=new Intent(context, TemperatureConfiguration.class);
                break;
            case "Lighting":
                i=new Intent(context, LightingConfiguration.class);
                break;
            case "Camera":
                i=new Intent(context, CameraConfiguration.class);
                break;
            default:
                Log.i(TAG,"Type not supported");
        }
        Log.i(TAG,deviceInformation.get(0).get(position)+deviceInformation.get(1).get(position)+deviceInformation.get(2).get(position)+deviceInformation.get(3).get(position)+deviceInformation.get(4).get(position));
        i.putExtra("NAME", deviceInformation.get(0).get(position));
        i.putExtra("IP", deviceInformation.get(1).get(position));
        i.putExtra("MAC", deviceInformation.get(2).get(position));
        i.putExtra("ROOM", deviceInformation.get(3).get(position));
        i.putExtra("TYPE", deviceInformation.get(4).get(position));
        return i;

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
