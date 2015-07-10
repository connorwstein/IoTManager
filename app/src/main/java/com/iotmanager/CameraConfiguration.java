package com.iotmanager;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.nio.charset.Charset;

import static com.iotmanager.Constants.*;


public class CameraConfiguration extends GenericConfiguration {
    private static final String TAG="Connors Debug";
    private TextView ipAddress;
    private TextView macAddress;
    private Button takePicture;
    private ImageView cameraPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_configuration);
        getDeviceInformation();
        initViews();
        deviceCommunicationHandler=new DeviceCommunicationHandler(ip,DEFAULT_DEVICE_TCP_PORT,this);
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
    }

    private void takePicture(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_TAKE_PICTURE);
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
        String rawImageData=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_GET_PICTURE);
        Log.i(TAG,rawImageData);
        //Log.i(TAG,"RAW IMAGE DATA: "+rawImageData.substring(0,100));
       // createJPEG(rawImageData);
    }

    private void createJPEG(String rawImageData){
        byte[] imageBytes=rawImageData.getBytes(Charset.forName("UTF-8"));
        Bitmap imageBitmap= BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
        cameraPicture.setImageBitmap(imageBitmap);

    }

}
