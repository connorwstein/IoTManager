package com.iotmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

import static com.iotmanager.Constants.COMMAND_CAMERA_GET_PICTURE;
import static com.iotmanager.Constants.COMMAND_CAMERA_STOP_PICTURE;

/**
 * Asynchronous task to get the image bytes from the esp8266
 * Created by connorstein on 15-07-14.
 */
public class GetPicture extends AsyncTask<Object,Integer,Integer> {
    private static final int MAXIMUM_ESP_PACKET=1460; //The esp8266 can only send data in chunks of this size
    private static final int SOCKET_TIMEOUT=5000; //5 second timeout
    private static final int MAX_PICTURE_SIZE=20000; //Maximum picture size 20000 bytes (somewhat arbitrary, depends on the dimensions & compression ratio selected by the user, some combinations will result in images larger than this size)
    private static final String TAG = "Connors Debug";
    private static final String END_IMAGE_TAG="FFD9"; //if the image is successfully received from the camera, the last 2 bytes will be FFD9
    private byte[] readBuffer=null;
    private String ip;
    private int port=80;
    private BufferedInputStream in;
    private ProgressDialog pd;
    private int size;
    private PrintWriter out;
    private ImageView image;
    private byte[] pictureBytes;
    private DeviceCommunicationHandler deviceCommunicationHandler;
    private TextView defaultText;
    private Handler handler;
    private Context context;

    @Override
    protected Integer doInBackground(Object... params) {
            size=(int)params[0];
            pd=(ProgressDialog)params[1];
            ip=(String)params[2];
            image=(ImageView)params[3];
            deviceCommunicationHandler=(DeviceCommunicationHandler) params[4];
            handler=(Handler)params[5];
            defaultText=(TextView)params[6];
            context=(Context)params[7];

            readBuffer=new byte[2*size]; //2*size because response from device will be a hex string i.e. AB1234CEF ... each char is a nibble
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
                out.write(COMMAND_CAMERA_GET_PICTURE);
                out.flush();
                in = new BufferedInputStream(socket.getInputStream(),readBuffer.length);
                int read=0;
                while(read<size){
                    read+=in.read(readBuffer,read,MAXIMUM_ESP_PACKET);
                    Log.i(TAG,"Read: "+read);
                }
                socket.close();
            } catch (IOException e) {
                Log.i(TAG, "IOException ");
            }
            return 0;
    }

//    //Unable to get this progress update to work, would be nice to show a % of how much of the image has been receivied)
//    @Override
//    protected void onProgressUpdate(Integer... values) {
//        pd.setProgress((int)((float)values[0]/(float)(readBuffer.length/MAXIMUM_ESP_PACKET)*100));
//    }

    @Override
    protected void onPostExecute(Integer error) {
        super.onPostExecute(error);
        pd.dismiss();
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
        if(error==-1){
            Toast.makeText(context, "Error receiving picture, make sure compression ratio is high enough and dimensions are small enough", Toast.LENGTH_SHORT).show();
            Log.i(TAG,"Error recieving picture data");
        }
        else{
            createJPEG();
        }
    }

    /**
     * Creates a jpeg from the bytes received and sets the imageview to it
     * Also passes the picturesBytes back to the CameraConfiguration activity for emailing
     */
    private void createJPEG() {
        int i=2*size-1;
        //Start at the last byte and loop until an F is seen (-1 in 2's complement)
        //Looking for the end of the image indicator FFD9 (if this is obtained the image has been successfully received)
        while(i>=0&&readBuffer[i]!=-1){
            i--;
        }
        try {
            if (i == 0 || !String.format("%02X%02X", readBuffer[i], readBuffer[i + 1]).equals(END_IMAGE_TAG)) {
                Log.i(TAG, "No FFD9 received");
                Toast.makeText(context, "Did not receive the whole picture. Try again.", Toast.LENGTH_SHORT);
                return;
            }
        }
        catch(Exception e){
            Log.i(TAG,"Exception read buffer is null");
        }
        Log.i(TAG, "End check: " + String.format("%02X%02X", readBuffer[i], readBuffer[i + 1])); //FFD9
        Log.i(TAG, "Start check " + String.format("%02X%02X", readBuffer[5], readBuffer[6])); //FFD8, skip the first 5 bytes which are a response success check

        int imageLength = (i + 1) - 5 + 1;//size of FFD8....FFD9 i.e. just the image itself
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(readBuffer, 5, (i + 2) - 5, null);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        image.setImageBitmap(imageBitmap);
        defaultText.setVisibility(View.INVISIBLE); //hide the default text while the image is there

        pictureBytes = new byte[imageLength];
        System.arraycopy(readBuffer, 5, this.pictureBytes, 0, imageLength);
        Message m = new Message();
        Bundle b = new Bundle();
        b.putByteArray("Image", pictureBytes);
        m.setData(b);
        handler.sendMessage(m); //send the picture bytes back to the CameraConfiguration for emailing
    }
}

