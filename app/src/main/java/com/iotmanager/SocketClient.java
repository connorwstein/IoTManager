package com.iotmanager;

import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;

import java.io.PrintWriter;
import java.net.Socket;

import static com.iotmanager.Constants.DEFAULT_DEVICE_IP;
import static com.iotmanager.Constants.DEFAULT_DEVICE_PORT;

/**
 * Created by connorstein on 15-06-03.
 */
public class SocketClient {
    private static final String TAG="Connors Debug";

    public static Thread tcpSend(final String data, final String IP, final int port,final ProgressDialog progressDialog, final Handler handler){
        Thread tcpSendThread=new Thread(new Runnable(){
            Socket s = null;
            PrintWriter out;
            @Override
            public void run() {
                try {
                    s = new Socket(IP, port);
                    out = new PrintWriter(s.getOutputStream());
                    out.write(data);
                    out.flush();
                } catch (Exception e) {
                    Log.i(TAG, "Exception connecting to socket: " + e.getMessage());
                }
                handler.sendEmptyMessage(0);
            }
        });
        return tcpSendThread;
    }

}
