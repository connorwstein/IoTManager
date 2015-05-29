package com.example.connorstein.sockethelloworld;

import android.os.AsyncTask;
import android.util.Log;

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
    public String deviceIPAddress=null;

    @Override
    protected String doInBackground(Void... args) {
        String responsePacketData=null;
        int sentPackets=0;
        while(responsePacketData==null&&sentPackets<maximumNumberSendPackets) {
            try {
                DatagramSocket ds = new DatagramSocket();
                Log.i(TAG, "Created datagram socket");
                byte sendBuffer[] = broadcastMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(broadcastAddress), broadCastPort);
                byte receiveBuffer[] = new byte[RECEIVE_BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                Log.i(TAG, "Sending mac address");
                ds.setSoTimeout(socketTimeout);
                ds.setBroadcast(true);
                ds.send(sendPacket);
                sentPackets++;
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

    }

    private static String getIPAddressFromResponsePacket(String responsePacketData){
        return responsePacketData.substring(2,responsePacketData.length());
    }
}
