package com.iotmanager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.iotmanager.Constants.DEFAULT_DEVICE_BROADCAST_IP;
import static com.iotmanager.Constants.DEFAULT_DEVICE_UDP_PORT;

/**
 * Created by connorstein on 15-06-08.
 */
public class DeviceCommunicationHandler {

    //UDP
    private static final int MAX_NUM_RECEIVE_PACKETS=6;
    private static final int MAX_NUM_SEND_PACKETS=6;
    private static final int UDP_SOCKET_TIMEOUT = 400;
    private static final int RECEIVE_BUFFER_SIZE=200;

    //TCP
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int DEFAULT_READ_BUF_SIZE=1024;
    private String deviceIP;
    private int devicePort;
    private PrintWriter out;
    private BufferedInputStream in;
    private byte[] readBuffer;
    private static final String TAG = "Connors Debug";
    private Context context;

    public DeviceCommunicationHandler(String deviceIP, int devicePort, Context context) {
        this.deviceIP = deviceIP;
        this.devicePort = devicePort;
        this.readBuffer = new byte[DEFAULT_READ_BUF_SIZE];
        this.context = context;
    }
    public void setIP(String newIP) {
        this.deviceIP = newIP;
    }

    public void setPort(int newPort) {
        this.devicePort = newPort;
    }

    public void sendDataNoResponse(final String data) {
        //Open a tcp connection with the device and send the data
        //must be in separate thread to avoid networkonmain thread error
        Log.i(TAG, "Send data no response");
        //Need handler to manipulate UI thread depending on what happens with the send
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                handleMessageResponse(message);
            }
        };
        Thread sendDataNoResponse = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = createSocket();
                    socketWrite(socket, data);
                    socket.close(); //Have to close, want to reopen new sockets for each method call
                } catch (Exception e) {
                    Log.i(TAG, "Exception " + e.getMessage());
                    handler.sendMessage(createMessage("Exception", e.getMessage()));
                }
                handler.sendMessage(createMessage("Exception", "None"));
            }
        });
        sendDataNoResponse.start();
        try {
            sendDataNoResponse.join();
        } catch (InterruptedException e) {
            Log.i(TAG, "Interrupted exception");
        }
        //block until thread is finished
    }


    private void flushReadBuffer() {
        int i;
        for (i = 0; i < readBuffer.length; i++) {
            readBuffer[i] = '\0';
        }
    }

    private void handleMessageResponse(Message message) {
        String exception = getStringFromMessage("Exception", message);
        if (exception == null) {
            Log.i(TAG, "null response");
            Toast.makeText(context, "Unable to send. Error: No Response", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!exception.equals("None")) {
            Toast.makeText(context, "Unable to send. Error: " + getStringFromMessage("Exception", message), Toast.LENGTH_LONG).show();
        }
    }

    private Message createMessage(String key, String data) {
        Message m = new Message();
        Bundle b = new Bundle();
        b.putString(key, data);
        m.setData(b);
        return m;
    }

    private String getStringFromMessage(String key, Message message) {
        return message.getData().getString(key);
    }



    //Returns Device objects based on which devices respond to the broadcast message sent
    //Used for determining which devices are on the network
    public static void broadcastForDevices(final String broadcastMessage,final Handler callback){
        //Log.i(TAG,"Broadcasting");
        Thread broadcastForDevices=new Thread(new Runnable(){
                @Override
                public void run(){
                    DatagramSocket udpBroadcastSocket=null;
                    try{
                        udpBroadcastSocket= new DatagramSocket();
                       // Log.i(TAG, "Created datagram socket");
                    }
                    catch(Exception e){
                        Log.i(TAG,"Exception "+e.getMessage());
                    }
                    ArrayList<Device>detectedDevices=processDeviceResponses(sendBroadcastPackets(udpBroadcastSocket,broadcastMessage));
                    Bundle b=new Bundle();
                    b.putSerializable("Devices",detectedDevices);
                    Message m=new Message();
                    m.setData(b);
                    callback.sendMessage(m);
                }
        });
        broadcastForDevices.start();
    }

    private static ArrayList<Device> processDeviceResponses(ArrayList<String>deviceResponses){
        final ArrayList<Device> devices=new ArrayList<>();
        for(String response:deviceResponses){
            devices.add(ResponseParser.createDeviceFromResponse(response));
        }
        ResponseParser.removeDuplicates(devices);
        return devices;
    }
    //Returns responses from devices (unprocessed)
    private static ArrayList<String> sendBroadcastPackets(DatagramSocket udpBroadcastSocket, String broadcastMessage){
        //sent MAX_NUM_SEND_PACKETS and assume responses will occur
        //to at least one
        //For each sent packet, attempt to receive MAX_NUM_RECEIVE_PACKETS
        //Gather all responses and remove duplicates if necessary
        Log.i(TAG,"Sending broadcast packets");
        ArrayList<String>deviceResponses=new ArrayList<>();
        try {
            udpBroadcastSocket.setSoTimeout(UDP_SOCKET_TIMEOUT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        int sentPackets=0;
        while(sentPackets<MAX_NUM_SEND_PACKETS) {
            try {
                byte sendBuffer[] = broadcastMessage.getBytes();
                sentPackets++;
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(DEFAULT_DEVICE_BROADCAST_IP), DEFAULT_DEVICE_UDP_PORT);
                byte receiveBuffer[] = new byte[RECEIVE_BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                String responsePacketData=null;
                udpBroadcastSocket.setBroadcast(true);
                //Log.i(TAG, "Sent broadcast packets");
                udpBroadcastSocket.send(sendPacket);
                udpBroadcastSocket.receive(receivePacket);
                responsePacketData= new String(receivePacket.getData(), 0, receivePacket.getLength());
                Log.i(TAG, "Received: " + responsePacketData);
                deviceResponses.add(responsePacketData);
                if(responsePacketData.contains("IP:")&&responsePacketData.contains("MAC:")&&responsePacketData.contains("NAME:")
                        && responsePacketData.contains("ROOM:")&&responsePacketData.contains("TYPE:")){
                    deviceResponses.add(responsePacketData);
                }
                else{
                    Log.i(TAG,"Invalid Packet: "+responsePacketData);
                }
            } catch (SocketTimeoutException e) {
                Log.i(TAG, "Socket timeout");
            }
            catch (IOException e) {
                Log.i(TAG, "IO exception");
            }
        }

        udpBroadcastSocket.close();
//        deviceResponses=receiveMultiplePackets(udpBroadcastSocket); //receiveMultiplePackets allocates memory for deviceResponses

        return deviceResponses;
    }

    //Tries to receive MAX_NUM_RECEIVE_PACKETS and returns device responses
    private static ArrayList<String> receiveMultiplePackets(DatagramSocket udpBroadcastSocket){
        ArrayList<String>deviceResponses=new ArrayList<>();
        int i;
        Log.i(TAG,"Receiving broadcast packets");
        for(i=0;i<MAX_NUM_RECEIVE_PACKETS;i++){
            String responsePacketData=null;
            byte receiveBuffer[] = new byte[RECEIVE_BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try{
                udpBroadcastSocket.receive(receivePacket);
            }
            catch(IllegalBlockingModeException e){
                Log.i(TAG,"Illegal blocking mode exception receiving packet "+i);
                continue;
            }
            catch(PortUnreachableException e){
                Log.i(TAG,"Port unreachable receiving packet "+i);
                continue;
            }
            catch(SocketTimeoutException e){
                Log.i(TAG,"Socket timeout exception receiving packet "+i);
                continue;
            }
            catch(IOException e){
                Log.i(TAG,"IO exception receiving packet "+i);
                continue;
            }

            //udpBroadcastSocket.close();
            // Log.i(TAG, "Packet received, length: "+receivePacket.getLength());
            responsePacketData= new String(receivePacket.getData(), 0, receivePacket.getLength());
            Log.i(TAG, "Received: " + responsePacketData);
            if(responsePacketData.contains("IP:")&&responsePacketData.contains("MAC:")&&responsePacketData.contains("NAME:")
                    && responsePacketData.contains("ROOM:")&&responsePacketData.contains("TYPE:")){
                deviceResponses.add(responsePacketData);
            }
            else{
                Log.i(TAG,"Invalid Packet: "+responsePacketData);
            }

        }
        return deviceResponses;
    }

    public String sendDataGetResponse(final String data) {
        //Open a tcp connection with the device and send the data
        //must be in separate thread to avoid networkonmain thread error

        //Need handle to manipulate UI thread depending on what happens with the send
        //Log.i(TAG,"Send data get response");
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                handleMessageResponse(message);
            }
        };
        Thread sendDataGetResponse = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = createSocket();
                    socketWrite(socket, data);
                    in = new BufferedInputStream(socket.getInputStream(),readBuffer.length);
                    in.read(readBuffer);
                    socket.close();
                } catch (IOException e) {
                    Log.i(TAG, "IOException");
                    handler.sendMessage(createMessage("Exception", e.getMessage()));
                    return;
                }
                handler.sendMessage(createMessage("Exception", "None"));

            }
        });
        sendDataGetResponse.start();
        try {
            sendDataGetResponse.join();
        } catch (InterruptedException e) {
            Log.i(TAG, "Interrupted exception");
        }
        //block until thread is finished
        return getStringFromReadBuffer();
    }



    private void socketWrite(Socket socket, String data) throws IOException {
        out = new PrintWriter(socket.getOutputStream());
        out.write(data);
        out.flush();
    }

    private Socket createSocket() throws IOException {
        Socket socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(deviceIP), devicePort);
        socket.connect(socketAddress, SOCKET_TIMEOUT);
        socket.setSoTimeout(SOCKET_TIMEOUT);
        return socket;
    }

    private String getStringFromReadBuffer() {
        if (readBuffer[0] == '\0') {
            //No response from device
            return null;
        }
        String result = null;
        try {
                int i = 0;
                while (readBuffer[i++] != '\0') ;
                result = new String(readBuffer, 0, i - 1, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "Exception " + e.getMessage());
        }
        flushReadBuffer();
        return result;
    }
}
