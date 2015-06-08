package com.iotmanager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * Created by connorstein on 15-06-08.
 */
public class DeviceCommunicationHandler {
    private static final int BUFFER_SIZE=1024;
    private String deviceIP;
    private int devicePort;
    private PrintWriter out;
    private BufferedInputStream in;
    private byte[] readBuffer;
    private static final String TAG="Connors Debug";
    private Context context;

    public DeviceCommunicationHandler(String deviceIP,int devicePort, Context context){
        this.deviceIP=deviceIP;
        this.devicePort=devicePort;
        readBuffer=new byte[BUFFER_SIZE];
        this.context=context;
    }

    public void sendDataNoResponse(final String data){
        //Open a tcp connection with the device and send the data
        //must be in separate thread to avoid networkonmain thread error
        final ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Sending ...");
        progressDialog.show();
        //Need handle to manipulate UI thread depending on what happens with the send
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                Log.i(TAG,getStringFromMessage("Exception",message));
                Toast.makeText(context,"Unable to send. Error "+getStringFromMessage("Exception",message),Toast.LENGTH_LONG).show();
            }
        };
        Thread sendDataNoResponse= new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Socket s=new Socket(deviceIP,devicePort);
                    out=new PrintWriter(s.getOutputStream());
                    out.write(data);
                    out.flush();
                    s.close();
                }
                catch(Exception e){
                    //Log.i(TAG,"Exception "+e.getMessage());
                    handler.sendMessage(createMessage("Exception",e.getMessage()));
                }

            }
        });
        sendDataNoResponse.start();
        //block until thread is finished
        try{
            sendDataNoResponse.join();
            progressDialog.dismiss();
        }
        catch(InterruptedException e){
            Log.i(TAG, "interrupted exception");
        }

    }

    private Message createMessage(String key, String data){
        Message m=new Message();
        Bundle b=new Bundle();
        b.putString(key,data);
        m.setData(b);
        return m;
    }

    private String getStringFromMessage(String key, Message message){
        return message.getData().getString(key);
    }

    public String sendDataGetResponse(final String data){
        //Open a tcp connection with the device and send the data
        //must be in separate thread to avoid networkonmain thread error
        final ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Sending ...");
        progressDialog.show();
        //Need handle to manipulate UI thread depending on what happens with the send
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                Log.i(TAG,getStringFromMessage("Exception",message));
                Toast.makeText(context,"Unable to send. Error: "+getStringFromMessage("Exception",message),Toast.LENGTH_SHORT).show();
            }
        };
        Thread sendDataNoResponse= new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Socket s=new Socket(deviceIP,devicePort);
                    out=new PrintWriter(s.getOutputStream());
                    out.write(data);
                    out.flush();
                    in=new BufferedInputStream(s.getInputStream());
                    in.read(readBuffer);
                }
                catch(Exception e){
                    //Log.i(TAG,"Exception "+e.getMessage());
                    handler.sendMessage(createMessage("Exception",e.getMessage()));
                }

            }
        });
        sendDataNoResponse.start();
        //block until thread is finished
        try{
            sendDataNoResponse.join();
            progressDialog.dismiss();
        }
        catch(InterruptedException e){
            Log.i(TAG, "interrupted exception");
        }
        String result=null;
        try{
            result=new String(readBuffer,0,20,"UTF-8");
        }
        catch(UnsupportedEncodingException e){
            Log.i(TAG,"Exception "+e.getMessage());
        }
        return result;
    }

    public void broadcastForDevices(){

    }


}
