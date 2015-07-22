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
    private Button takePicture;
    private ImageView cameraPicture;
    private ProgressDialog pg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_configuration);
        getDeviceInformation();
        initViews();
        deviceCommunicationHandler=new DeviceCommunicationHandler(ip,DEFAULT_DEVICE_TCP_PORT,this,MAX_IMAGE_SIZE);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private void initViews(){
        setTitle(name);
        takePicture=(Button)findViewById(R.id.cameraTakePicture);
        cameraPicture=(ImageView)findViewById(R.id.cameraPicture);
        if(Build.VERSION.SDK_INT<21){
            cameraPicture.setImageDrawable(getResources().getDrawable(R.drawable.camera));
        }
        else{
            cameraPicture.setImageDrawable(getResources().getDrawable(R.drawable.camera,getTheme()));
        }
        pg=new ProgressDialog(this);
        pg.setTitle("Taking picture...");
    }

    private void takePicture(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_TAKE_PICTURE);
        Log.i(TAG, "Response " + response);
        if(response==null){
            Log.i(TAG,"Error receiving response from device");
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
            return;
        }
        if(response.equals(RESPONSE_TAKE_PICTURE_SUCCESS)){
            Log.i(TAG, response);
            getPictureSize();
        }
        else if(response.equals(RESPONSE_TAKE_PICTURE_FAIL)){
            Log.i(TAG,"Device unable to take a picture");
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
        }
        else{
            Log.i(TAG,"Firmware malfunction, received junk response");
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);

        }
    }
    private void getPictureSize(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_GET_SIZE);
        Log.i(TAG, "Response " + response);
        if(response!=null){
            getPicture(Integer.parseInt(response));
        }
        else{
            Log.i(TAG,"Error receiving response from device");
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
        }
    }
    private void getPicture(int size){
//        deviceCommunicationHandler.setPictureBoolean(true);
//        String rawImageData=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_GET_PICTURE);
//        Log.i(TAG, rawImageData);
//        deviceCommunicationHandler.setPictureBoolean(false);
        ProgressDialog p=new ProgressDialog(this);
        p.setMessage("Taking picture...");
        p.setIndeterminate(false);
        p.show();
        GetPicture getPictureTask=new GetPicture();
        getPictureTask.execute(size,p,ip.toString(),cameraPicture,deviceCommunicationHandler);
//        createJPEG(rawImageData);
    }

}
