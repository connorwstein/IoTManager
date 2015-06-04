package com.iotmanager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
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
                Message msg=new Message();
                Bundle bundle=new Bundle();
                try {
                    s = new Socket(IP, port);
                    out = new PrintWriter(s.getOutputStream());
                    out.write(data);
                    out.flush();
                } catch (Exception e){
                    Log.i(TAG,"IOexception in socket");
                    bundle.putInt("Error code", 0);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    return;
                }
                bundle.putInt("Error code",1);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
        return tcpSendThread;
    }

}
