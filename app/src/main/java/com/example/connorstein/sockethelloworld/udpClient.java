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

        try{

            DatagramSocket ds=new DatagramSocket();
            Log.i(TAG,"Created datagram socket");
            InetAddress addr=InetAddress.getByName("255.255.255.255");
            Log.i(TAG,"IP: "+addr);
            byte sendbuf[]="HELLO 18:fe:34:9f:d7:ca".getBytes();

            DatagramPacket dp=new DatagramPacket(sendbuf,sendbuf.length,addr,1025);
            Log.i(TAG, "Sending: "+"hello".getBytes());
            ds.setBroadcast(true);
            ds.send(dp);

        }
        catch(Exception e){
            Log.i(TAG,"Exception has occured: "+e.getMessage());
        }
        return "done";

    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.i(TAG, "Received: " + result);

    }

}
