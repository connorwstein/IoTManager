package com.example.connorstein.sockethelloworld;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient extends AsyncTask<Void, Integer, String> {
    public static final int BUFFER_SIZE = 4096;
    private static final String TAG="sure2015test";
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedInputStream in = null;
    private int port = 0;
    private String host = null;
    private String data=null;
    private Context context;
    private String ssid=null;
    private String password=null;
    private WifiManager manager=null;
    public TcpClient(String host,int port,String data,Context context, String ssid,String password,WifiManager manager) {
        this.host=host;
        this.port=port;
        this.data=data;
        this.context=context;
        this.password=password;
        this.ssid=ssid;
        this.manager=manager;
    }

    @Override
    protected String doInBackground(Void... args) {

        if(send(data)!=0){
            return null;
        }
        Log.i(TAG, "Sent: " + data);
        String response=receive();

        return response;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        Log.i(TAG, "Received: " + response);
        if(response==null){
            return;
        }
        disconnect();
        AddDevice.connect(ssid, password, context, manager); //Blocks until connected
        UdpClient clientToGetIP=new UdpClient(context);
        clientToGetIP.execute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if(values==null)Toast.makeText(context,"Unable to connect, ensure password is correct",Toast.LENGTH_LONG).show();

    }

    private int connect() {
        try {
            if (socket == null) {
                socket = new Socket(this.host, this.port);
                out = new PrintWriter(socket.getOutputStream());
                in = new BufferedInputStream(socket.getInputStream());
            }
        }
        catch (IOException e) {
            Log.i(TAG, "IO Exeception trying to connect");
            publishProgress(null);
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
        } catch (IOException e) {
            Log.i(TAG,"IO Error in receiving message");
            return null;
        }
    }
}
