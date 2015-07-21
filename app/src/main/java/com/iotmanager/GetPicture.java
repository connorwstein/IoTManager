package com.iotmanager;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.lang.Byte;

import static com.iotmanager.Constants.COMMAND_CAMERA_STOP_PICTURE;

/**
 * Created by connorstein on 15-07-14.
 */
public class GetPicture extends AsyncTask<Object,Integer,Integer> {
    private static final int MAXIMUM_ESP_PACKET=1460;
    private static final int SOCKET_TIMEOUT=5000;
    private static final int MAX_PICTURE_SIZE=14000;
    private static final String TAG = "Connors Debug";
    private byte[] readBuffer;
    private String ip;
    private int port=80;
    private BufferedInputStream in;
    private ProgressDialog pd;
    private int size;
    private PrintWriter out;
    private ImageView image;
    private DeviceCommunicationHandler deviceCommunicationHandler;
    @Override
    protected Integer doInBackground(Object... params) {
            size=(int)params[0];
            pd=(ProgressDialog)params[1];
            ip=(String)params[2];
            image=(ImageView)params[3];
            deviceCommunicationHandler=(DeviceCommunicationHandler) params[4];
            readBuffer=new byte[2*size];
            if(size>MAX_PICTURE_SIZE){
                Log.i(TAG,"Maximum picture size exceeded");
                return -1;
            }

            try {
                Socket socket = new Socket();
                SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
                socket.connect(socketAddress, SOCKET_TIMEOUT);
                socket.setSoTimeout(SOCKET_TIMEOUT);
                out=new PrintWriter(socket.getOutputStream());
                out.write("Camera Get Picture");
                out.flush();
                in = new BufferedInputStream(socket.getInputStream(),readBuffer.length);
                int read=0;
                while(read<=size){
                    read+=in.read(readBuffer,read,MAXIMUM_ESP_PACKET);
                    Log.i(TAG,"Read: "+read);
                }
                socket.close();
            } catch (IOException e) {
                Log.i(TAG, "IOException "); //Always throws this exception, not sure why
            }
            return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        pd.setProgress((int)((float)values[0]/(float)(readBuffer.length/MAXIMUM_ESP_PACKET)*100));
    }

    @Override
    protected void onPostExecute(Integer error) {
        super.onPostExecute(error);
        //use string builder when looping, much faster than concatenating
        StringBuilder sb=new StringBuilder();
        for (byte b : readBuffer) {
            sb.append(String.format("%02X", b));
        }
        Log.i(TAG, "Image Data: " + sb.toString());
        pd.dismiss();
        if(error==-1){
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
            Log.i(TAG,"Error recieving picture data");
        }
        else{
            createJPEG(readBuffer);
        }
    }
    private void createJPEG(byte[] rawData) {

        int i=2*size-1;
        while(i>=0&&readBuffer[i]!=-1){
            i--;
        }
        if(i==0){
            Log.i(TAG,"No FFD9 received");
            return;
        }
        Log.i(TAG,"End check: "+String.format("%02X%02X",readBuffer[i],readBuffer[i+1]));
        Log.i(TAG,"Start check "+String.format("%02X%02X",readBuffer[5],readBuffer[6]));
        Bitmap imageBitmap= BitmapFactory.decodeByteArray(readBuffer, 5, (i+2)-5, null);
        image.setImageBitmap(imageBitmap);
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
    }
}

