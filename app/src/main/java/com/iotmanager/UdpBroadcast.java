package com.iotmanager;

import static com.iotmanager.Constants.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

    @Override
    protected Boolean doInBackground(Object... args) {
        context=(Context)args[0];
        progressDialog=(ProgressDialog)args[1];
        devicesListView=(ListView)args[2];
        deviceCategory=(String)args[3];
        broadcastMessage="Hello "+deviceCategory+" Devices?";
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
        }
        //List 0: Names, List 1: IPs, List 2: MAC adddresses
        //Want to display only the names, and pass the rest to the device configuration activity
        //so if user clicks on a name more detailed information is available
        final ArrayList<ArrayList<String>>deviceInformation=getDistinctDeviceInformation(deviceResponses);

        ArrayAdapter<String> deviceNameAdapter=new ArrayAdapter<String>(
                context,
                R.layout.list, //device list has size of text in the listView
                deviceInformation.get(0) //the first list has the names
        );
        devicesListView.setAdapter(deviceNameAdapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent startDeviceConfiguration = createIntentWithDeviceInformation(deviceCategory, deviceInformation, position);
                if (startDeviceConfiguration != null) {
                    context.startActivity(startDeviceConfiguration);
                }
                else{
                    Toast.makeText(context, "Device configuration data error",Toast.LENGTH_LONG);
                }

            }
        });

    }
    private Intent createIntentWithDeviceInformation(String type, final ArrayList<ArrayList<String>>deviceInformation, int position){
        Log.i(TAG, "Create Device info intent");
        Intent i=null;
//        if(!type.equals(deviceInformation.get(4).get(position))){
//            //Type does not match, must be an error
//            return i;
//        }
        switch(type){
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
    //Removes duplicate responses
    //Parses responses for NAME, IP, MAC
    //Returns arraylist of arraylists where list 0: names, list 1: ips, list 3: mac addresses list 4 rooms, list 5 types
    private ArrayList<ArrayList<String>> getDistinctDeviceInformation(ArrayList<String> deviceResponses){
        ArrayList<String> deviceNames=new ArrayList<String>();
        ArrayList<String> deviceIPs=new ArrayList<String>();
        ArrayList<String> deviceMACs=new ArrayList<String>();
        ArrayList<String> deviceRooms=new ArrayList<String>();
        ArrayList<String> deviceTypes=new ArrayList<String>();
        Set<String> distinctDeviceResponses=new HashSet<>();
        distinctDeviceResponses.addAll(deviceResponses);
        deviceResponses.clear();
        deviceResponses.addAll(distinctDeviceResponses);
        for (String deviceResponse: deviceResponses){
            deviceNames.add(getDeviceName(deviceResponse));
            deviceIPs.add(getDeviceIP(deviceResponse));
            deviceMACs.add(getDeviceMAC(deviceResponse));
            deviceRooms.add(getDeviceRoom(deviceResponse));
            deviceTypes.add(getDeviceType(deviceResponse));
        }
        ArrayList<ArrayList<String>> devicesInformation=new ArrayList<ArrayList<String>>();
        devicesInformation.add(deviceNames);
        devicesInformation.add(deviceIPs);
        devicesInformation.add(deviceMACs);
        devicesInformation.add(deviceRooms);
        devicesInformation.add(deviceTypes);
        return devicesInformation;
    }

    //Takes "Name:...IP:...MAC:..." response and extracts the NAME
    //Assume name does not have IP or MAC in it...
    private String getDeviceName(String deviceResponse){
        int indexOfName=deviceResponse.indexOf("NAME:");
        int charsInName=5;
        int indexOfIP=deviceResponse.indexOf("IP:");
        return deviceResponse.substring(indexOfName+charsInName,indexOfIP);
    }

    //Takes "Name:...IP:...MAC:..." response and extracts the IP
    //Assume name does not have IP or MAC in it...
    private String getDeviceIP(String deviceResponse){
        int indexOfIP=deviceResponse.indexOf("IP:");
        int charsInIP=3;
        int indexOfMAC=deviceResponse.indexOf("MAC:");
        return deviceResponse.substring(indexOfIP+charsInIP,indexOfMAC);
    }

    //Takes "Name:...IP:...MAC:..." response and extracts the MAC
    //Assume name does not have IP or MAC in it...
    private String getDeviceMAC(String deviceResponse){
        int indexOfMAC=deviceResponse.indexOf("MAC:");
        int charsInMac=4;
        int indexOfRoom=deviceResponse.indexOf("ROOM:");
        return deviceResponse.substring(indexOfMAC+charsInMac,indexOfRoom);
    }
    //"Name:....IP:....MAC:...Room:....Type:....
    private String getDeviceRoom(String deviceResponse){
        int indexOfRoom=deviceResponse.indexOf("ROOM:");
        int charsInRoom=5;
        int indexOfType=deviceResponse.indexOf("TYPE:");
        return deviceResponse.substring(indexOfRoom+charsInRoom,indexOfType);
    }
    private String getDeviceType(String deviceResponse){
        int indexOfType=deviceResponse.indexOf("TYPE:");
        int charsInType=5;
        return deviceResponse.substring(indexOfType+charsInType);
    }

}
