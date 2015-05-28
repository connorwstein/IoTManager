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
public class udpClient extends AsyncTask<Void,Void,String> {

    private static final String TAG="sure2015test";
    @Override
    protected String doInBackground(Void... args) {
        String received=null;
        while(received==null) {
            try {

                DatagramSocket ds = new DatagramSocket();
                Log.i(TAG, "Created datagram socket");
                InetAddress addr = InetAddress.getByName("255.255.255.255");
                Log.i(TAG, "IP: " + addr);
                byte sendbuf[] = "HELLO 18:fe:34:9f:d7:ca".getBytes();
                DatagramPacket dp = new DatagramPacket(sendbuf, sendbuf.length, addr, 1025);
                byte result[] = new byte[256];
                DatagramPacket dpresp = new DatagramPacket(result, result.length);
                Log.i(TAG, "Sending mac address");
                ds.setSoTimeout(2000);
                ds.setBroadcast(true);
                ds.send(dp);
                ds.receive(dpresp);
                Log.i(TAG, "Past received");
                received = new String(dpresp.getData(), 0, dpresp.getLength());

            } catch (Exception e) {
                Log.i(TAG, "Exception has occured: " + e.getMessage());
            }
        }
        return received;

    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.i(TAG, "Received: " + result);

    }

}
