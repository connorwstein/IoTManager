package com.example.connorstein.IoTManager;

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
public class GetIpViaUdpBroadcast extends AsyncTask<Object,Void,Integer> {
    private String broadcastAddress="255.255.255.255";
    private int broadCastPort=1025 ; //must be greater than 1024
    private String broadcastMessage;
    private static final int RECEIVE_BUFFER_SIZE=1024;
    private int MAX_NUM_RECEIVE_PACKETS=10;
    private static final String TAG="sure2015test";
    private int socketTimeout = 100;
    private int maximumNumberSendPackets=3;
    private ProgressDialog progressDialog;
    private Context context;
    private ArrayList<String> deviceResponses;
    private ListView devicesListView;
    private String deviceType;
    @Override
    protected Integer doInBackground(Object... args) {
        context=(Context)args[0];
        progressDialog=(ProgressDialog)args[1];
        devicesListView=(ListView)args[2];
        deviceType=(String)args[3];
        broadcastMessage="Hello "+deviceType+" Devices?";
        deviceResponses=new ArrayList<String>();
        int sentPackets=0;
        DatagramSocket ds=null;
        try{
            ds = new DatagramSocket();
            Log.i(TAG, "Created datagram socket");
        }
        catch(Exception e){
            Log.i(TAG,"Exception "+e.getMessage());
        }

        while(sentPackets<maximumNumberSendPackets) {
            try {
                byte sendBuffer[] = broadcastMessage.getBytes();
                sentPackets++;
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(broadcastAddress), broadCastPort);
                Log.i(TAG, "Sending broadcast message");
                ds.setSoTimeout(socketTimeout);
                ds.setBroadcast(true);
                ds.send(sendPacket);
                receiveMultiplePackets(ds);
            } catch (Exception e) {
                Log.i(TAG, "Exception has occured: " + e.getMessage());
            }
        }
        Log.i(TAG,"Number of devices that responded "+deviceResponses.size());
        if(deviceResponses.size()==0){
            return -1;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer success) {
        super.onPostExecute(success);
        progressDialog.dismiss();
        if(success==-1){
            Log.i(TAG, "No devices on this network");
            Toast.makeText(context,"No devices on this network.",Toast.LENGTH_LONG).show();

        }
        //List 1: Names, List 2: IPs, List 3: Macs
        //Want to display the names, and pass the rest to the device configuration activity
        //if user clicks on a name
        final ArrayList<ArrayList<String>>deviceInformation=getDistinctDeviceInformation(deviceResponses);
        for(String i: deviceInformation.get(0)){
            Log.i(TAG,"Device "+i);
        }
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                context,
                R.layout.device_list,
                deviceInformation.get(0)
        );
        devicesListView.setAdapter(arrayAdapter);
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent startDeviceConfiguration = new Intent(context, DeviceConfiguration.class);
                startDeviceConfiguration.putExtra("IP", deviceInformation.get(1).get(position));
                startDeviceConfiguration.putExtra("MAC", deviceInformation.get(2).get(position));
                startDeviceConfiguration.putExtra("NAME", deviceInformation.get(0).get(position));
                context.startActivity(startDeviceConfiguration);
            }
        });

    }


    private ArrayList<ArrayList<String>> getDistinctDeviceInformation(ArrayList<String> deviceResponses){
        ArrayList<String> deviceNames=new ArrayList<String>();
        ArrayList<String> deviceIPs=new ArrayList<String>();
        ArrayList<String> deviceMACs=new ArrayList<String>();
        Set<String> distinctDeviceResponses=new HashSet<>();
        distinctDeviceResponses.addAll(deviceResponses);
        deviceResponses.clear();
        deviceResponses.addAll(distinctDeviceResponses);
        Log.i(TAG,"Device responses size "+deviceResponses.size());
        for(String deviceResponse: deviceResponses){
            deviceNames.add(getDeviceName(deviceResponse));
            deviceIPs.add(getDeviceIP(deviceResponse));
            deviceMACs.add(getDeviceMAC(deviceResponse));
            Log.i(TAG,"Device response "+deviceResponse);
            Log.i(TAG,"Device name "+getDeviceName(deviceResponse));
            Log.i(TAG,"Device ip "+getDeviceIP(deviceResponse));
            Log.i(TAG,"Device mac "+getDeviceMAC(deviceResponse));
        }
        ArrayList<ArrayList<String>> devicesInformation=new ArrayList<ArrayList<String>>();
        devicesInformation.add(deviceNames);
        devicesInformation.add(deviceIPs);
        devicesInformation.add(deviceMACs);
        return devicesInformation;
    }

    private String getDeviceIP(String deviceResponse){
        //Takes "Name:...IP:...MAC:..." response and extracts the IP
        //Assume name does not have IP or MAC in it...
        int indexOfIP=deviceResponse.indexOf("IP:");
        int charsInIP=3;
        int indexOfMAC=deviceResponse.indexOf("MAC:");
        return deviceResponse.substring(indexOfIP+charsInIP,indexOfMAC);
    }

    private String getDeviceMAC(String deviceResponse){
        int indexOfMAC=deviceResponse.indexOf("MAC:");
        int charsInMac=4;
        return deviceResponse.substring(indexOfMAC+charsInMac);
    }

    private String getDeviceName(String deviceResponse){
        int indexOfName=deviceResponse.indexOf("NAME:");
        int charsInName=5;
        int indexOfIP=deviceResponse.indexOf("IP:");
        return deviceResponse.substring(indexOfName+charsInName,indexOfIP);
    }

    private void receiveMultiplePackets(DatagramSocket ds){
        int i;
        for(i=0;i<MAX_NUM_RECEIVE_PACKETS;i++){
            String responsePacketData=null;
            byte receiveBuffer[] = new byte[RECEIVE_BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try{
                ds.receive(receivePacket);
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

}
