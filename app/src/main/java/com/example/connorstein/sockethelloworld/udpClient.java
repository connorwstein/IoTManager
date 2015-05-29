package com.example.connorstein.sockethelloworld;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by connorstein on 15-05-28.
 */
public class UdpClient extends AsyncTask<Void,Void,String> {
    private String broadcastAddress="255.255.255.255";
    private int broadCastPort=1025 ; //must be greater than 1024
    private String broadcastMessage="HELLO 18:fe:34:9f:d7:ca";
    private static final int RECEIVE_BUFFER_SIZE=1024;
    private static final String TAG="sure2015test";
    private int socketTimeout = 3000;
    private int maximumNumberSendPackets=5;
    private final String SAVED_DEVICES_FILE="ESP_DEVICES";

    public String deviceIPAddress=null;
    private Context context;
    public UdpClient(Context context){
        this.context=context;
    }
    @Override
    protected String doInBackground(Void... args) {
        String responsePacketData=null;
        int sentPackets=0;
        DatagramSocket ds=null;
        try{
            ds = new DatagramSocket();
            Log.i(TAG, "Created datagram socket");
        }
        catch(Exception e){
            Log.i(TAG,"Exception "+e.getMessage());
        }

        while(responsePacketData==null&&sentPackets<maximumNumberSendPackets) {
            try {

                byte sendBuffer[] = broadcastMessage.getBytes();
                sentPackets++;
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(broadcastAddress), broadCastPort);
                byte receiveBuffer[] = new byte[RECEIVE_BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                Log.i(TAG, "Sending mac address");
                ds.setSoTimeout(socketTimeout);
                ds.setBroadcast(true);
                ds.send(sendPacket);

                ds.receive(receivePacket);
                Log.i(TAG, "Packet received, length: "+receivePacket.getLength());
                responsePacketData= new String(receivePacket.getData(), 0, receivePacket.getLength());

            } catch (Exception e) {
                Log.i(TAG, "Exception has occured: " + e.getMessage());

            }
        }
        if(sentPackets==maximumNumberSendPackets){
            Log.i(TAG,"Unable to obtain ip address");

        }
        return responsePacketData;
    }
    @Override
    protected void onPostExecute(String responsePacketData) {
        super.onPostExecute(responsePacketData);
        Log.i(TAG, "Received: " + responsePacketData);
        Toast.makeText(context, "Received IP ", Toast.LENGTH_LONG).show();
        FileOutputStream fos;
        try{
            fos=context.openFileOutput(SAVED_DEVICES_FILE,Context.MODE_PRIVATE);
            fos.write((getIPAddressFromResponsePacket(responsePacketData)+"\n").getBytes());
            fos.close();
        }
        catch(Exception e){
            Log.i(TAG,"UDP ON POST Exception: "+e.getMessage());
        }
    }


    private static String getIPAddressFromResponsePacket(String responsePacketData){
        return responsePacketData.substring(2,responsePacketData.length());
    }
}
