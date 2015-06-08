package com.iotmanager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;




/**
 * Created by connorstein on 15-06-03.
 */
public class SocketClient {
    private static final String TAG="Connors Debug";
    private static final int SOCKET_READ_BUFFER_SIZE=1024;
    private static Socket socket=null;

    public static Thread tcpSend(final String data, final String IP, final int port,final ProgressDialog progressDialog, final Handler handler){
        Thread tcpSendThread=new Thread(new Runnable(){
            PrintWriter out;
            @Override
            public void run() {
                Message msg=new Message();
                Bundle bundle=new Bundle();
                try {
                    socket = new Socket(IP, port);
                    out = new PrintWriter(socket.getOutputStream());
                    out.write(data);
                    out.flush();
                } catch (Exception e){
                    Log.i(TAG,"Exception: "+e.getMessage());
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

    public static Thread tcpReceive(ProgressDialog progressDialog,final Handler handler){
        Thread tcpReceiveThread=new Thread(new Runnable(){
            @Override
            public void run(){
                BufferedInputStream reader;
                byte[] receiveBuffer =new byte[SOCKET_READ_BUFFER_SIZE];
                String received="";
                try{
                    reader=new BufferedInputStream(socket.getInputStream());
                    reader.read(receiveBuffer);
                    Log.i(TAG,receiveBuffer.toString());
                    int i;
                    for(i=0;i<SOCKET_READ_BUFFER_SIZE&&receiveBuffer[i]!=0;i++){} //Loop until null char
                    received=new String(receiveBuffer,0,i,"UTF-8"); //return a String created from the non-null chars received

                }
                catch(Exception e){
                    Log.i(TAG,"Exception "+e.getMessage());
                }
                Message msg= new Message();
                Bundle bundle=new Bundle();
                bundle.putString("Received",received);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
        return tcpReceiveThread;
    }

    public static void closeConnection(){
        try{
            socket.close();
        }
        catch(Exception e){
            Log.i(TAG,"Exception "+e.getMessage());
        }
    }

}
