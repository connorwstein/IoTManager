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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by connorstein on 15-06-08.
 */
public class DeviceCommunicationHandler {
    private static final int BUFFER_SIZE=1024;
    private static final int SOCKET_TIMEOUT=2000;
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

    public void setIP(String newIP){
        this.deviceIP=newIP;
    }
    public void setPort(int newPort){
        this.devicePort=newPort;
    }
    public void sendDataNoResponse(final String data){
        //Open a tcp connection with the device and send the data
        //must be in separate thread to avoid networkonmain thread error
        Log.i(TAG,"Send data no response");
        //Need handler to manipulate UI thread depending on what happens with the send
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                handleMessageResponse(message);
            }
        };
        Thread sendDataNoResponse= new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Socket socket=createSocket();
                    socketWrite(socket,data);
                    socket.close(); //Have to close, want to reopen new sockets for each method call
                }
                catch(Exception e){
                    //Log.i(TAG,"Exception "+e.getMessage());
                    handler.sendMessage(createMessage("Exception",e.getMessage()));
                }
                handler.sendMessage(createMessage("Exception","None"));
            }
        });
        sendDataNoResponse.start();
        try{
            sendDataNoResponse.join();
        }
        catch(InterruptedException e){
            Log.i(TAG,"Interrupted exception");
        }
        //block until thread is finished
    }

    private void flushReadBuffer(){
        int i;
        for(i=0;i<BUFFER_SIZE;i++){
            readBuffer[i]='\0';
        }
    }
    private void handleMessageResponse(Message message){
        String exception=getStringFromMessage("Exception",message);
        if(exception==null){
            Log.i(TAG,"null response");
            Toast.makeText(context,"Unable to send. Error: No Response",Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(TAG, exception);
        if(!exception.equals("None")){
            Toast.makeText(context,"Unable to send. Error: "+getStringFromMessage("Exception",message),Toast.LENGTH_SHORT).show();
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

        //Need handle to manipulate UI thread depending on what happens with the send
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message message){
                handleMessageResponse(message);
            }
        };
        Thread sendDataGetResponse= new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Socket socket=createSocket();
                    socketWrite(socket,data);
                    in=new BufferedInputStream(socket.getInputStream());
                    in.read(readBuffer);
                    socket.close();
                }
                catch(Exception e){
                    //Log.i(TAG,"Exception "+e.getMessage());
                    handler.sendMessage(createMessage("Exception",e.getMessage()));
                    return;
                }
                handler.sendMessage(createMessage("Exception","None"));

            }
        });
        sendDataGetResponse.start();
        try{
            sendDataGetResponse.join();
        }
        catch(InterruptedException e){
            Log.i(TAG,"Interrupted exception");
        }
        //block until thread is finished
        return getStringFromReadBuffer();
    }
    private void socketWrite(Socket socket, String data) throws Exception{
        out=new PrintWriter(socket.getOutputStream());
        out.write(data);
        out.flush();
    }

    private Socket createSocket() throws Exception {
        Socket socket=new Socket();
        SocketAddress socketAddress=new InetSocketAddress(InetAddress.getByName(deviceIP),devicePort);
        socket.connect(socketAddress,SOCKET_TIMEOUT);
        socket.setSoTimeout(SOCKET_TIMEOUT);
        return socket;
    }

    private String getStringFromReadBuffer(){
        if(readBuffer[0]=='\0'){
            //No response from device
            return null;
        }
        String result=null;
        try{
            int i=0;
            while(readBuffer[i++]!='\0');
            result=new String(readBuffer,0,i-1,"UTF-8");
        }
        catch(UnsupportedEncodingException e){
            Log.i(TAG,"Exception "+e.getMessage());
        }
        flushReadBuffer();
        return result;
    }
}
