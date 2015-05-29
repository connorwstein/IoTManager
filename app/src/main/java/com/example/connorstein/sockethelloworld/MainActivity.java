package com.example.connorstein.sockethelloworld;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="sure2015test";
    private static final String SAVED_DEVICES_FILE="ESP_DEVICES";
    private static final int FILE_READ_BUF_SIZE=1024;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        byte readBuf[]=new byte[FILE_READ_BUF_SIZE];
        try{
            FileInputStream fis=openFileInput(SAVED_DEVICES_FILE);
            fis.read(readBuf,0,FILE_READ_BUF_SIZE);
            Log.i(TAG, "Reading back data from file " + new String(readBuf, "UTF-8"));
            fis.close();
        }
        catch(FileNotFoundException e){
            Log.i(TAG,"File does not exist yet, must create");
            File savedDevices=new File(Context.getFilesDir(),)
            createNewFile();
        }
        catch(Exception e){
            Log.i(TAG, "Exception: " + e.getMessage());
            Toast.makeText(getApplicationContext(),"No saved devices",Toast.LENGTH_LONG).show();

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.add_device:
                Intent intent=new Intent(MainActivity.this,AddDevice.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
