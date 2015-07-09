package com.iotmanager;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
        ipAddress.setText(ip);
        macAddress.setText(mac);
    }

    private void takePicture(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_TAKE_PICTURE);
        if(response!=null){
            Log.i(TAG, "Response from taking picture "+response);
            getPicture();
        }
        else{
            Log.i(TAG,"Null response when taking a picture");
        }

    }
    private void getPicture(){
        String rawImageData=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_GET_PICTURE);
        Log.i(TAG,"RAW IMAGE DATA: "+rawImageData.substring(0,100));
        createJPEG(rawImageData);
    }

    private void createJPEG(String rawImageData){
        byte[] imageBytes=rawImageData.getBytes(Charset.forName("UTF-8"));
        Bitmap imageBitmap= BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
        ImageView picture =new ImageView(this);
        picture.setImageBitmap(imageBitmap);

    }

}
