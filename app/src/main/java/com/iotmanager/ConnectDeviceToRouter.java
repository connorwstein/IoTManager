package com.iotmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectDeviceToRouter extends AsyncTask<Object, Integer, Integer> {
    private static final String TAG="Connors Debug";
    public static final int BUFFER_SIZE = 4096;
    private static final int DEFAULT_DEVICE_PORT = 80;
    private static final String DEFAULT_DEVICE_IP = "192.168.4.1";
    private Socket socket = null;
    private PrintWriter out = null;
    private String data=null;
    private Context context=null;
    private Network network=null;
    private ProgressDialog progressDialog=null;

    @Override
    protected Integer doInBackground(Object... args) {
        network=(Network)args[0];
        context=(Context)args[1];
        progressDialog=(ProgressDialog)args[2];
        data="Connect:"+network.ssid+";"+network.password;
        if(send(data)!=0){
            progressDialog.dismiss();
            return -1;
        }
        Log.i(TAG, "Sent: " + data);
        return 0;
    }

    @Override
    protected void onPostExecute(Integer success) {
        super.onPostExecute(success);
        if(success==-1){
            progressDialog.dismiss();
            Toast.makeText(context,"Unable to tell device to connect, ensure password is correct. It may be required to reconnect to the device again on the previous page.",Toast.LENGTH_LONG).show();
            return;
        }
        disconnect();
        //progressDialog.setMessage("Device connected to router");
        Log.i(TAG,"device connected to router");
        boolean connectAndStartDeviceActivity=false;
        //Now device is connected to desired router, now connect phone to router
        ConnectAndroid connectRequest=new ConnectAndroid();
        connectRequest.execute(network,context,progressDialog,connectAndStartDeviceActivity);
    }


    private int connect() {
        try {
            if (socket == null) {
                socket = new Socket(DEFAULT_DEVICE_IP,DEFAULT_DEVICE_PORT);
                out = new PrintWriter(socket.getOutputStream());
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

}
