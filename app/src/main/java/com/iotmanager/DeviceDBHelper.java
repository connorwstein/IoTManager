package com.iotmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.iotmanager.DevicesDBContract.DevicesDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connorstein on 15-07-17.
 * Handles all database transactions for the devices.
 * Note the use of .trim() when working with strings to remove whitespace, all information stored in the database should have no leading or trailing whitespace
 * All tests for the database are in the DeviceDBTest class
 */
public class DeviceDBHelper extends SQLiteOpenHelper {
    private static final String TAG="Connors Debug";
    private static final String TEXT_TYPE = " text";
    private static final String COMMA_SEP = ", ";
    //Note the DB does not store IP address as this may change due to DHCP
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + DevicesDB.TABLE_NAME + " (" +
                    DevicesDB.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                    DevicesDB.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    DevicesDB.COLUMN_ROOM + TEXT_TYPE + COMMA_SEP +
                    DevicesDB.COLUMN_TYPE + TEXT_TYPE + COMMA_SEP +
                    DevicesDB.COLUMN_MAC + TEXT_TYPE +
            ");";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DevicesDB.TABLE_NAME;

    public static final String DATABASE_NAME="Devices";
    public static final int DATABASE_VERSION=1;

    public DeviceDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //upgrade policy of deleting all data and starting over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        //Log.i(TAG, "Database opened");
    }

    public int addDevice(Device device){
        String name=device.getName();
        String room=device.getRoom();
        String type=device.getType();
        String mac=device.getMac();
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "
                +DevicesDB.COLUMN_NAME +"="+"'"+name.trim()+"'"+" AND "
                +DevicesDB.COLUMN_ROOM +"="+"'"+room.trim()+"'"+" AND "
                +DevicesDB.COLUMN_TYPE +"="+"'"+type.trim()+"'"+" AND "
                +DevicesDB.COLUMN_MAC +"="+"'"+mac.trim()+"'",null);

        if(c.moveToFirst()){
            Log.i(TAG, "Device already exists: " + c.getString(0) + ", " + c.getString(1) + ", " + c.getString(2) + ", " + c.getString(3));
            return -1;
        }
        ContentValues values= new ContentValues();
        values.put(DevicesDB.COLUMN_NAME, name.trim());
        values.put(DevicesDB.COLUMN_ROOM, room.trim());
        values.put(DevicesDB.COLUMN_TYPE, type.trim());
        values.put(DevicesDB.COLUMN_MAC, mac.trim());
        db.insert(DevicesDB.TABLE_NAME, null, values);
        //dumpDBtoLog();
        db.close();
        return 0;
    }

    public int updateDevice(int idOldDevice,Device updatedDevice){
        String updatedName=updatedDevice.getName();
        String updatedRoom=updatedDevice.getRoom();
        String updatedType=updatedDevice.getType();
        String updatedMac=updatedDevice.getMac();
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "
                +DevicesDB.COLUMN_ID +"="+idOldDevice,null);
        if(!c.moveToFirst()){
            Log.i(TAG,"No such device: "+idOldDevice+", can not change to "+updatedName.trim()+", "+updatedRoom.trim()+", "+updatedType.trim()+", "+updatedMac);
            //dumpDBtoLog();
            return -1;
        }
        else{
            ContentValues values= new ContentValues();
            values.put(DevicesDB.COLUMN_NAME, updatedName.trim());
            values.put(DevicesDB.COLUMN_ROOM, updatedRoom.trim());
            values.put(DevicesDB.COLUMN_TYPE, updatedType.trim());
            values.put(DevicesDB.COLUMN_MAC, updatedMac.trim());
            db.update(DevicesDB.TABLE_NAME, values, DevicesDB.COLUMN_ID + "=" + idOldDevice, null);
            return 0;
        }
    }
    public void emptyDB(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
        db.close();
    }
    public void deleteDevice(Device device){
        String name=device.getName();
        String room=device.getRoom();
        String type=device.getType();
        String mac=device.getMac();
        SQLiteDatabase db=this.getWritableDatabase();
        try{
            db.delete(DevicesDB.TABLE_NAME, DevicesDB.COLUMN_NAME + "=" + "'" + name.trim() + "'"+" AND "+
                                            DevicesDB.COLUMN_ROOM + "=" + "'" + room.trim() + "'"+" AND "+
                                            DevicesDB.COLUMN_TYPE + "=" + "'" + type.trim() + "'"+" AND "+
                                            DevicesDB.COLUMN_MAC + "=" + "'" + mac.trim() + "'", null);
        }
        catch(SQLiteException e){
            Log.i(TAG,"No columns available to delete");
        }
        //dumpDBtoLog();
        db.close();
    }

    public int getID(Device device){
        String name =device.getName();
        String room = device.getRoom();
        String type = device.getType();
        String mac =device.getMac();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "
                +DevicesDB.COLUMN_NAME +"="+"'"+name.trim()+"'"+" AND "
                +DevicesDB.COLUMN_ROOM +"="+"'"+room.trim()+"'"+" AND "
                +DevicesDB.COLUMN_TYPE +"="+"'"+type.trim()+"'"+" AND "
                +DevicesDB.COLUMN_MAC +"="+"'"+mac.trim()+"'",null);

        if(!c.moveToFirst()){
            Log.i(TAG,"No such device exists: "+name.trim()+", "+room.trim()+", "+type.trim()+", "+mac.trim());
            //dumpDBtoLog();
            return -1;
        }
        else{
            return Integer.parseInt(c.getString(0));
        }
    }
    public Device getDevice(int id){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "
                +DevicesDB.COLUMN_ID +"="+id,null);
        if(!c.moveToFirst()){
            Log.i(TAG,"No device with id "+ id);
            return null;
        }
        else{
            return new Device(
                    c.getString(c.getColumnIndex(DevicesDB.COLUMN_NAME)),
                    null,
                    c.getString(c.getColumnIndex(DevicesDB.COLUMN_MAC)),
                    c.getString(c.getColumnIndex(DevicesDB.COLUMN_ROOM)),
                    c.getString(c.getColumnIndex(DevicesDB.COLUMN_TYPE))
            );
        }
    }
    public String getRoomFromMac(String mac){
        SQLiteDatabase db=this.getReadableDatabase();
        Log.i(TAG,"Getting room from mac "+mac.trim());
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "+DevicesDB.COLUMN_MAC+"="+"'"+mac.trim()+"'",null);
        String result=null;
        if(c.moveToFirst()){
            result=c.getString(c.getColumnIndex(DevicesDB.COLUMN_ROOM));
        }
        else{
            Log.i(TAG,mac+ " does not exist in db");
        }
        return result;
    }

    public void dumpDBtoLog(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM " + DevicesDB.TABLE_NAME + ";", null);
        Log.i(TAG, "DATABASE CHANGE");
        Log.i(TAG, "ID, NAME, ROOM, TYPE");
        if(c.moveToFirst()){
            do{
                StringBuilder sb=new StringBuilder();
                int numColumns=c.getColumnCount();
                for(int i=0;i<numColumns;i++){
                    if(i==numColumns-1){
                        sb.append(c.getString(i));
                    }
                    else{
                        sb.append(c.getString(i)+", ");
                    }
                }
                Log.i(TAG,sb.toString());
            }
            while(c.moveToNext());
        }
        db.close();
    }
}
