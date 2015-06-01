package com.example.connorstein.sockethelloworld;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectDeviceToRouter extends AsyncTask<Object, Integer, Integer> {
    public static final int BUFFER_SIZE = 4096;
    private static final String TAG="sure2015test";
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedInputStream in = null;
    private int defaultDevicePort = 80;
    private String defaultDeviceIP = "192.168.4.1";
    private String data=null;
    private Context context=null;
    private Network network=null;
    private ProgressDialog progressDialog=null;
    @Override
    protected Integer doInBackground(Object... args) {
        network=(Network)args[0];
        context=(Context)args[1];
        progressDialog=(ProgressDialog)args[2];
        data=network.ssid+";"+network.password;
        if(send(data)!=0){
            progressDialog.dismiss();
            return -1;
        }
        Log.i(TAG, "Sent: " + data);
        String response=receive();
        Log.i(TAG, "Received: " + response);
        return 0;
    }

    @Override
    protected void onPostExecute(Integer success) {
        super.onPostExecute(success);
        if(success==-1){
            Toast.makeText(context,"Unable to tell device to connect",Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            return;
        }
        disconnect();
        //progressDialog.setMessage("Device connected to router");
        Log.i(TAG,"device connected to router");
        boolean connectAndStartDeviceActivity=false;
        //Now device is connected to desired router, now connect phone to router
        Connect connectRequest=new Connect();
        connectRequest.execute(network,context,progressDialog,connectAndStartDeviceActivity);
    }



    private int connect() {
        try {
            if (socket == null) {
                socket = new Socket(defaultDeviceIP,defaultDevicePort);
                out = new PrintWriter(socket.getOutputStream());
                in = new BufferedInputStream(socket.getInputStream());
            }
        }
        catch (IOException e) {
            Log.i(TAG, "IO Exeception trying to connect");
            return -1;
        }
        Log.i(TAG, "Connected");
        return 0;
    }

    private void disconnect() {
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    Log.i(TAG, "IO exception disconnecting");
                }
            }
        }
        Log.i(TAG, "Disconnected");
    }

    public int send(String message) {
        if (message != null) {
            if(connect()!=0){
                return -1;
            }
            out.write(message);
            out.flush();
            return 0;
        }
        Log.i(TAG,"send failed");
        return -1;
    }

    public String receive() {
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            in.read(buf,0,BUFFER_SIZE);
            int i;
            for(i=0;i<BUFFER_SIZE&&buf[i]!=0;i++){} //Loop until null char
            return new String(buf,0,i,"UTF-8"); //return a String created from the non-null chars received
        }
        catch (IOException e) {
            Log.i(TAG,"IO Error in receiving message");
            return null;
        }
    }
}
