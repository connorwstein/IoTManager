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

    private static final String BROADCAST_ADDRESS="255.255.255.255";
    private static final int BROADCAST_PORT=1025 ; //must be greater than 1024
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
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(BROADCAST_ADDRESS), DEFAULT_DEVICE_UDP_PORT);
                Log.i(TAG, "Sending broadcast message");
                udpBroadcastSocket.setSoTimeout(SOCKET_TIMEOUT);
                udpBroadcastSocket.setBroadcast(true);
                udpBroadcastSocket.send(sendPacket);
                receiveMultiplePackets(udpBroadcastSocket);
            } catch (Exception e) {
                Log.i(TAG, "Exception has occured: " + e.getMessage());
            }
        }
        Log.i(TAG,"Number of devices that responded "+ deviceResponses.size());
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
            Toast.makeText(context,"No devices on this network. Try broadcasting again and ensure the correct password was sent to device when connecting it to the network.",Toast.LENGTH_LONG).show();
        }
        //List 0: Names, List 1: IPs, List 2: MAC adddresses
        //Want to display only the names, and pass the rest to the device configuration activity
        //so if user clicks on a name more detailed information is available
        final ArrayList<ArrayList<String>>deviceInformation=getDistinctDeviceInformation(deviceResponses);

        ArrayAdapter<String> deviceNameAdapter=new ArrayAdapter<String>(
                context,
                R.layout.device_list, //device list has size of text in the listView
                deviceInformation.get(0) //the first list has the names
        );
        devicesListView.setAdapter(deviceNameAdapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(deviceCategory){
                    case "Temperature":
                        Intent deviceConfigurationIntent = new Intent(context, TemperatureConfiguration.class);
                        deviceConfigurationIntent.putExtra("NAME", deviceInformation.get(0).get(position));
                        deviceConfigurationIntent.putExtra("IP", deviceInformation.get(1).get(position));
                        deviceConfigurationIntent.putExtra("MAC", deviceInformation.get(2).get(position));
                        context.startActivity(deviceConfigurationIntent);
                        break;
                    case "Lighting":
                        Intent lightingConfigurationIntent = new Intent(context, LightingConfiguration.class);
                        lightingConfigurationIntent.putExtra("NAME", deviceInformation.get(0).get(position));
                        lightingConfigurationIntent.putExtra("IP", deviceInformation.get(1).get(position));
                        lightingConfigurationIntent.putExtra("MAC", deviceInformation.get(2).get(position));
                        context.startActivity(lightingConfigurationIntent);
                        break;
                    default:
                        Log.i(TAG,"Error device category not supported");
                }

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
                Log.i(TAG,"IO exception receiving packet "+i);
                continue;
            }
            Log.i(TAG, "Packet received, length: "+receivePacket.getLength());
            responsePacketData= new String(receivePacket.getData(), 0, receivePacket.getLength());
            Log.i(TAG, "Received: " + responsePacketData);
            deviceResponses.add(responsePacketData);
        }
    }
    //Removes duplicate responses
    //Parses responses for NAME, IP, MAC
    //Returns arraylist of arraylists where list 0: names, list 1: ips, list 3: mac addresses
    private ArrayList<ArrayList<String>> getDistinctDeviceInformation(ArrayList<String> deviceResponses){
        ArrayList<String> deviceNames=new ArrayList<String>();
        ArrayList<String> deviceIPs=new ArrayList<String>();
        ArrayList<String> deviceMACs=new ArrayList<String>();
        Set<String> distinctDeviceResponses=new HashSet<>();
        distinctDeviceResponses.addAll(deviceResponses);
        deviceResponses.clear();
        deviceResponses.addAll(distinctDeviceResponses);
        for (String deviceResponse: deviceResponses){
            deviceNames.add(getDeviceName(deviceResponse));
            deviceIPs.add(getDeviceIP(deviceResponse));
            deviceMACs.add(getDeviceMAC(deviceResponse));
        }
        ArrayList<ArrayList<String>> devicesInformation=new ArrayList<ArrayList<String>>();
        devicesInformation.add(deviceNames);
        devicesInformation.add(deviceIPs);
        devicesInformation.add(deviceMACs);
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
        return deviceResponse.substring(indexOfMAC+charsInMac);
    }

}
