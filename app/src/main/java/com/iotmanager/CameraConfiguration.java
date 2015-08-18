package com.iotmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.iotmanager.Constants.*;

/**
 * Configuration specific for the Link Sprite Camera (https://www.sparkfun.com/products/retired/10061)
 * Provide functionality of taking pictures and emailing them
 * Note that you likely have to reduce image dimensions and/or compression size to take a picture (should probably update the defaults)
 */
public class CameraConfiguration extends GenericConfiguration {
    private static final String TAG="Connors Debug";
    private static final int COMPRESSION_SIZE_MENU_ID=10; //arbitrary id number, just used to add to the menu defined in GenericConfiguration
    private static final int IMAGE_DIMENSIONS_MENU_ID=11; //again arbitrary id number
    private static final int MAX_COMPRESSION_SIZE=255;
    private static final CharSequence[] ALLOWED_IMAGE_DIMENSIONS={"640x480","320x240","160x120"};

    private Button takePicture;
    private ImageView cameraPicture;
    private ProgressDialog pg;
    private Button emailPicture;
    private byte[] pictureBytes=null;
    private Handler handler;
    private TextView defaultText;

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
                pg.show();
                //set to false so that user cannot keep hitting the take picture button
                //Camera needs time between take picture commands
                pg.setCancelable(false);
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
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.findItem(COMPRESSION_SIZE_MENU_ID) == null) {
            menu.add(0, COMPRESSION_SIZE_MENU_ID, 0, "Change compression size"); //arbitrary id of 10, just used to check if item already exists in menu
            //check if null to solve bug where menu item keeps getting added
        }
        if (menu.findItem(IMAGE_DIMENSIONS_MENU_ID) == null) {
            menu.add(0, IMAGE_DIMENSIONS_MENU_ID, 0, "Change image dimensions"); //arbitrary id of 11, just used to check if item already exists in menu
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==COMPRESSION_SIZE_MENU_ID){
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
                        //ensure a correct value was entered [0-255]
                        if (!enteredValue.equals("") && value >= 0 && value <= MAX_COMPRESSION_SIZE) {
                            String response = deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_CHANGE_COMPRESSION + enteredValue);
                            if (response == null) {
                                Log.i(TAG, "Error communicating with device");
                                Toast.makeText(CameraConfiguration.this, "Error communicating with device", Toast.LENGTH_SHORT).show();

                            } else if (response.equals(RESPONSE_CAMERA_CHANGE_COMPRESSION_FAIL)) {
                                Log.i(TAG, "Error setting the compression ratio on device. Received: " + response);
                                Toast.makeText(CameraConfiguration.this, "Device failed to change compression ratio, try again.", Toast.LENGTH_SHORT).show();

                            } else if (response.equals(RESPONSE_CAMERA_CHANGE_COMPRESSION_SUCCESS)) {
                                Toast.makeText(CameraConfiguration.this, "Compression ratio changed to " + value, Toast.LENGTH_SHORT).show();
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
        else if(item.getItemId()==IMAGE_DIMENSIONS_MENU_ID){
            AlertDialog.Builder builder = new AlertDialog.Builder(CameraConfiguration.this)
                    .setTitle("Select Image Dimensions")
                    .setItems(ALLOWED_IMAGE_DIMENSIONS, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String response = deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_CHANGE_IMAGE_SIZE + which);
                            if (response == null) {
                                Toast.makeText(CameraConfiguration.this, "Error communicating with the device", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (response.equals(RESPONSE_CAMERA_CHANGE_IMAGE_SIZE_FAIL)) {
                                Toast.makeText(CameraConfiguration.this, "Camera failed to change image size", Toast.LENGTH_SHORT).show();
                                return;
                            } else if (response.equals(RESPONSE_CAMERA_CHANGE_IMAGE_SIZE_SUCCESS)) {
                                Toast.makeText(CameraConfiguration.this, "Image size changed to "+ALLOWED_IMAGE_DIMENSIONS[which], Toast.LENGTH_SHORT).show();
                                Intent homeIntent = new Intent(CameraConfiguration.this, AllDevices.class);
                                startActivity(homeIntent);
                            }
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize all views
     */
    private void initViews(){
        setTitle(device.getName());
        takePicture=(Button)findViewById(R.id.cameraTakePicture);
        emailPicture=(Button)findViewById(R.id.cameraEmailPicture);
        cameraPicture=(ImageView)findViewById(R.id.cameraPicture);
        cameraPicture.setBackgroundColor(getResources().getColor(R.color.lightblue));
        defaultText=(TextView)findViewById(R.id.defaultCameraText);
        pg=new ProgressDialog(this);
        pg.setMessage("Taking picture...");
    }

    /**
     * Sends take picture response and if successful, starts a chain of helper methods to obtain and display the image.
     * Note that if the take picture fails for any reason, we must send the stop picture command
     * so that the next time an image is taken it will be a new one (i.e. if stop picture is not called, the
     * camera will keep sending the bytes of the same image)
     */
    private void takePicture(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_TAKE_PICTURE);
        Log.i(TAG, "Response Take Picture: " + response);
        if(response==null){
            Log.i(TAG,"Error receiving response from device");
            Toast.makeText(CameraConfiguration.this, "Did not receive the whole picture. Try again.", Toast.LENGTH_SHORT);
            pg.dismiss();
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
            return;
        }
        if(response.equals(RESPONSE_TAKE_PICTURE_SUCCESS)){
            Log.i(TAG, response);
            //Now camera has taken the picture, get the size of the image to know
            //how large to make the buffer when obtaining the actual image bytes
            getPictureSize();
        }
        else if(response.equals(RESPONSE_TAKE_PICTURE_FAIL)){
            Log.i(TAG,"Device unable to take a picture");
            Toast.makeText(CameraConfiguration.this, "Device unable to take picture", Toast.LENGTH_SHORT).show();
            pg.dismiss();
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
        }
        else{
            Log.i(TAG,"Firmware malfunction, received junk response");
            Toast.makeText(CameraConfiguration.this, "Device firmware malfunction, received junk response", Toast.LENGTH_SHORT).show();
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
        }
    }

    /**
     * Gets the size of the picture taken and if successful asks the camera to send the raw bytes of the image
     * The response from the device is the size of the image in bytes
     */
    private void getPictureSize(){
        String response=deviceCommunicationHandler.sendDataGetResponse(COMMAND_CAMERA_GET_SIZE);
        Log.i(TAG, "Response Get Picture Size " + response);
        if(response!=null){
            getPicture(Integer.parseInt(response));
        }
        else{
            Log.i(TAG,"Error receiving response from device");
            Toast.makeText(CameraConfiguration.this,"Error getting picture size from device",Toast.LENGTH_SHORT).show();
            pg.dismiss();
            deviceCommunicationHandler.sendDataNoResponse(COMMAND_CAMERA_STOP_PICTURE);
        }
    }

    /**
     * Starts the async task GetPicture which handles the socket communciation with the device obtains the images bytes
     * @param size Size of picture in bytes
     */
    private void getPicture(int size){
        //Use an async task to get the picture bytes
        GetPicture getPictureTask=new GetPicture();
        //When the bytes have been obtained set the pictureBytes array (used for emailing the picture)
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                Log.i(TAG,"Getting picture bytes");
                pictureBytes=msg.getData().getByteArray("Image");
            }
        };
        getPictureTask.execute(size, pg, device.getIp(), cameraPicture, deviceCommunicationHandler, handler, defaultText, CameraConfiguration.this);
    }

    /**
     * Emails a picture (using androids email client)
     * Picture is first written to external storage with filename IMG_yyyy_mm_dd_hh_mm_ss
     * then attached to the email.
     * It will stay in external storage until the next image is taken, when the next image is taken cleanOutOldImages() is called
     * Do not want to save all the images otherwise it would eat up external storage without the user knowing
     */
    private void emailPicture(){
        if(pictureBytes!=null){
            Log.i(TAG,"Image: "+pictureBytes);
        }
        else{
            Log.i(TAG,"Picture bytes are null");
            Toast.makeText(CameraConfiguration.this,"Take a picture first!",Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText emailAddress=new EditText(CameraConfiguration.this);
        emailAddress.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        AlertDialog.Builder builder= new AlertDialog.Builder(CameraConfiguration.this)
                .setMessage("Enter email address")
                .setView(emailAddress)
                .setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String filename=createImageFileName();
                        cleanOutOldImages();
                        File file=createImageFile(filename);
                        Intent i=createEmailIntentWithPictureAttachment(emailAddress.getText().toString(),file);
                        try {
                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(CameraConfiguration.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                        Log.i(TAG,"Deleting file");
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    private void cleanOutOldImages(){
        String[] children = CameraConfiguration.this.getExternalFilesDir(null).list();
        for (int i = 0; i < children.length; i++) {
            Log.i(TAG,"Deleting: "+children[i]);
            new File(CameraConfiguration.this.getExternalFilesDir(null), children[i]).delete();
        }
    }

    private String createImageFileName(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String filename="IMG_"+dateFormat.format(Calendar.getInstance().getTime())+".jpg";
        return filename;
    }

    private File createImageFile(String filename){
        //NOTE MUST WRITE TO EXTERNAL STORAGE OTHERWISE GMAIL WILL NOT SEND ATTACHMENT
        File file = new File(CameraConfiguration.this.getExternalFilesDir(null),filename);
        Log.i(TAG,"Camera image file path: "+file.getAbsolutePath());
        file.setReadable(true);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(pictureBytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private Intent createEmailIntentWithPictureAttachment(String emailAddress,File file){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("vnd.android.cursor.dir/email");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        i.putExtra(Intent.EXTRA_SUBJECT, "Image from my cam");
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        return i;
    }
}









































































































































































































































































































































































































































































