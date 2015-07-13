package com.iotmanager;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

import static com.iotmanager.Constants.*;


public class CameraConfiguration extends GenericConfiguration {
    private static final String TAG="Connors Debug";
    private static final int MAX_IMAGE_SIZE=14000;
    private TextView ipAddress;
    private TextView macAddress;
    private Button takePicture;
    private ImageView cameraPicture;
    private ProgressDialog pg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_configuration);
        getDeviceInformation();
        initViews();
        deviceCommunicationHandler=new DeviceCommunicationHandler(ip,DEFAULT_DEVICE_TCP_PORT,this,MAX_IMAGE_SIZE,pg);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private void initViews(){
        setTitle(name);
        ipAddress=(TextView)findViewById(R.id.cameraIpAddress);
        macAddress=(TextView)findViewById(R.id.cameraMacAddress);
        takePicture=(Button)findViewById(R.id.cameraTakePicture);
        cameraPicture=(ImageView)findViewById(R.id.cameraPicture);
        if(Build.VERSION.SDK_INT<21){
            cameraPicture.setImageDrawable(getResources().getDrawable(R.drawable.camera));
        }
        else{
            cameraPicture.setImageDrawable(getResources().getDrawable(R.drawable.camera,getTheme()));
        }
        ipAddress.setText(ip);
        macAddress.setText(mac);
        pg=new ProgressDialog(this);
        pg.setTitle("Taking picture...");
    }

    private void takePicture(){
        pg.show();
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_TAKE_PICTURE);
        Log.i(TAG, "Response " + response);
        if(response.equals(RESPONSE_TAKE_PICTURE_SUCCESS)){
            Log.i(TAG, response);
            getPicture();
        }
        else if(response.equals(RESPONSE_TAKE_PICTURE_FAIL)){
            Log.i(TAG,"Device unable to take a picture");
        }
        else{
            Log.i(TAG,"Error receiving response from device");
        }

    }
    private void getPicture(){
        deviceCommunicationHandler.setRawData(true);
        String rawImageData=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_GET_PICTURE);
        Log.i(TAG, rawImageData);
        deviceCommunicationHandler.setRawData(false);
        //Log.i(TAG,"RAW IMAGE DATA: "+rawImageData.substring(0,100));
        createJPEG(rawImageData);
    }
    private void createJPEG(String rawImageData) {
        Log.i(TAG,"Response check : "+rawImageData.substring(0,14));
        int i=27999;
        while(rawImageData.charAt(i)!='7') {
            i--;
        }
        Log.i(TAG,"End check: "+rawImageData.substring(i-4,i+10));
        Log.i(TAG,"Start "+rawImageData.charAt(10)+ " End "+rawImageData.charAt(i-4));
        byte[] imageBytes=hexStringToByteArray(rawImageData.substring(10,i));
//        int i=0;
//        while(i<10){
//            Log.i(TAG, Arrays.toString(imageBytes));
//            i++;
//        }
        Bitmap imageBitmap= BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
        cameraPicture.setImageBitmap(imageBitmap);
        pg.dismiss();
        deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);

    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
//    private static byte[] hexStringToByteArray(String response){
//
//    }

}
