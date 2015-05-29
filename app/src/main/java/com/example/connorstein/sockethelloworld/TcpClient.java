package com.example.connorstein.sockethelloworld;

import android.content.Context;
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
    public TcpClient(String host,int port,String data,Context context) {
        this.host=host;
        this.port=port;
        this.data=data;
        this.context=context;
    }

    @Override
    protected String doInBackground(Void... args) {

        send(data);
        publishProgress(null);
        Log.i(TAG, "Sent: " + data);
        String response=receive();
        return response;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        Log.i(TAG, "Received: " + response);
        disconnect();
        UdpClient clientToGetIP=new UdpClient();
        clientToGetIP.execute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Toast.makeText(context,"Telling device to connect ... ", Toast.LENGTH_LONG).show();
    }

    private void connect() {
        try {
            if (socket == null) {
                socket = new Socket(this.host, this.port);
                out = new PrintWriter(socket.getOutputStream());
                in = new BufferedInputStream(socket.getInputStream());
            }
        }
        catch (IOException e) {
            Log.i(TAG, "IO Exeception trying to connect");
            return;
        }
        Log.i(TAG, "Connected");
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

    public void send(String message) {
        if (message != null) {
            connect();
            out.write(message);
            out.flush();
            return;
        }
        Log.i(TAG,"send failed");
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
            return "Error receiving response";
        }
    }
}
