package com.iotmanager;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import static com.iotmanager.Constants.*;


public class CameraConfiguration extends GenericConfiguration {
    private static final String TAG="Connors Debug";
    private static final int MAX_IMAGE_SIZE=14000;
    private Button takePicture;
    private ImageView cameraPicture;
    private ProgressDialog pg;
    private Button emailPicture;
    private byte[] pictureBytes=null;
    private Handler handler;
    private TextView defaultText;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(menu.findItem(10)==null){
            menu.add(0,10,0,"Change compression size"); //arbitrary id of 10, just used to check if item already exists in menu
            //solves bug where menu item keeps getting added
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==10){
            //Possible compression 0-255
            final EditText compressionValue=new EditText(CameraConfiguration.this);
            AlertDialog.Builder builder=new AlertDialog.Builder(CameraConfiguration.this)
                    .setMessage("Enter compression size [0-255]")
                    .setView(compressionValue)
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog dialog=builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String enteredValue = compressionValue.getText().toString();
                    try {
                        int value = Integer.parseInt(enteredValue);
                        if (!enteredValue.equals("") && value >= 0 && value <= 255) {
                            String response = deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_CHANGE_COMPRESSION + enteredValue);
                            if (response == null) {
                                Log.i(TAG, "Error communicating with device");
                                Toast.makeText(CameraConfiguration.this, "Error communicating with device", Toast.LENGTH_SHORT).show();

                            } else if (response.equals(RESPONSE_CAMERA_CHANGE_COMPRESSION_FAIL)) {
                                Log.i(TAG, "Error setting the compression ratio on device. Received: " + response);
                                Toast.makeText(CameraConfiguration.this, "Device failed to change compression ratio, try again.", Toast.LENGTH_SHORT).show();

                            } else if (response.equals(RESPONSE_CAMERA_CHANGE_COMPRESSION_SUCCESS)) {
                                Toast.makeText(CameraConfiguration.this, "Compression ratio changed to "+value, Toast.LENGTH_SHORT).show();

                                Log.i(TAG, "Compression ratio changed");
                            } else {
                                Toast.makeText(CameraConfiguration.this, "Device firmware malfunction, try again", Toast.LENGTH_SHORT).show();

                                Log.i(TAG, "Junk response from device");
                            }
                            dialog.cancel();
                        } else {
                            Toast.makeText(CameraConfiguration.this, "Please enter a valid compression value", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(CameraConfiguration.this, "Please enter a valid compression value", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_configuration);
        getDeviceInformation();
        initViews();
        deviceCommunicationHandler=new DeviceCommunicationHandler(device.getIp(),DEFAULT_DEVICE_TCP_PORT,this);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        emailPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailPicture();
            }
        });

    }
    private void emailPicture(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Calendar cal = Calendar.getInstance();
        String datetime=dateFormat.format(cal.getTime());
        Log.i(TAG,"Datetime "+datetime);
        Log.i(TAG,"Picture bytes 0: "+pictureBytes[0]);
        if(pictureBytes!=null){
            Log.i(TAG,"Image: "+pictureBytes);
        }
        else{
            Log.i(TAG,"Picture bytes are null");
            return;
        }
        String filename="IMG_"+datetime;
        final File file = new File(CameraConfiguration.this.getFilesDir(),filename);
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(file);
            fos.write(pictureBytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[]results=new byte[100];
        try{
            FileInputStream fin=new FileInputStream(file);
            fin.read(results);
        }
       catch(Exception e){
           Log.i(TAG,"File not found");
       }
        Log.i(TAG,"Test readback "+String.format("%02X%02X",results[0],results[1]));

        final EditText emailAddress=new EditText(CameraConfiguration.this);
        emailAddress.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        AlertDialog.Builder builder= new AlertDialog.Builder(CameraConfiguration.this)
                .setMessage("Enter email address")
                .setView(emailAddress)
                .setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress.getText().toString()});
                        i.putExtra(Intent.EXTRA_SUBJECT, "Image from my cam");
                        i.putExtra(Intent.EXTRA_TEXT   , "Hello world");
//                        Uri uri = Uri.fromFile(file);
//                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        try {
                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(CameraConfiguration.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                        file.delete();
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        file.delete();
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    private void initViews(){
        setTitle(device.getName());
        takePicture=(Button)findViewById(R.id.cameraTakePicture);
        emailPicture=(Button)findViewById(R.id.cameraEmailPicture);
        cameraPicture=(ImageView)findViewById(R.id.cameraPicture);
        cameraPicture.setBackgroundColor(Color.parseColor("#FFFFFF"));
        defaultText=(TextView)findViewById(R.id.defaultCameraText);
        pg=new ProgressDialog(this);
        pg.setTitle("Taking picture...");
    }

    private void takePicture(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_TAKE_PICTURE);
        Log.i(TAG, "Response Take Picture: " + response);
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
            Toast.makeText(CameraConfiguration.this, "Device unable to take picture", Toast.LENGTH_SHORT).show();

            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
        }
        else{
            Log.i(TAG,"Firmware malfunction, received junk response");
            Toast.makeText(CameraConfiguration.this, "Device firmware malfunction, received junk response", Toast.LENGTH_SHORT).show();

            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);

        }
    }
    private void getPictureSize(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_GET_SIZE);
        Log.i(TAG, "Response Get Picture Size " + response);
        if(response!=null){
            getPicture(Integer.parseInt(response));
        }
        else{
            Log.i(TAG,"Error receiving response from device");
            Toast.makeText(CameraConfiguration.this,"Error getting picture size from device",Toast.LENGTH_SHORT).show();
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
        }
    }
    private void getPicture(int size){

        ProgressDialog p=new ProgressDialog(this);
        p.setMessage("Taking picture...");
        p.setIndeterminate(false);
        p.show();
        GetPicture getPictureTask=new GetPicture();
        handler=new Handler(){
          @Override
            public void handleMessage(Message msg){
              Log.i(TAG,"Getting picture bytes");
                pictureBytes=msg.getData().getByteArray("Image");
          }
        };
        getPictureTask.execute(size,p,device.getIp(),cameraPicture,deviceCommunicationHandler,handler,defaultText);
    }

}
