package com.example.connorstein.sockethelloworld;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by connorstein on 15-05-28.
 */
public class GetIpViaUdpBroadcast extends AsyncTask<Object,Void,Integer> {
    private String broadcastAddress="255.255.255.255";
    private int broadCastPort=1025 ; //must be greater than 1024
    private String broadcastMessage="Hello Espressif Devices?";
    private static final int RECEIVE_BUFFER_SIZE=1024;
    private int MAX_NUM_RECEIVE_PACKETS=10;
    private static final String TAG="sure2015test";
    private int socketTimeout = 100;
    private int maximumNumberSendPackets=3;
    private ProgressDialog progressDialog;
    private Context context;
    private ArrayList<String> devices;
    private ListView devicesListView;

    @Override
    protected Integer doInBackground(Object... args) {
        context=(Context)args[0];
        progressDialog=(ProgressDialog)args[1];
        devicesListView=(ListView)args[2];
        devices=new ArrayList<String>();
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
        if(devices.size()==0){
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
        Set<String> hs=new HashSet<>();
        hs.addAll(devices);
        devices.clear();
        devices.addAll(hs);
        for(String i: devices){
            Log.i(TAG,"Device "+i);
        }
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                context,
                R.layout.device_list,
                devices
        );
        devicesListView.setAdapter(arrayAdapter);

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
            devices.add(responsePacketData);
        }

    }

}
