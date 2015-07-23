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
 */
public class DeviceDBHelper extends SQLiteOpenHelper {
    private static final String TAG="Connors Debug";
    private static final String TEXT_TYPE = " text";
    private static final String COMMA_SEP = ", ";
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

    public int addDevice(String name, String room, String type, String mac){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "
                +DevicesDB.COLUMN_NAME +"="+"'"+name.trim()+"'"+" AND "
                +DevicesDB.COLUMN_ROOM +"="+"'"+room.trim()+"'"+" AND "
                +DevicesDB.COLUMN_TYPE +"="+"'"+type.trim()+"'"+" AND "
                +DevicesDB.COLUMN_MAC +"="+"'"+mac.trim()+"'",null);

        if(c.moveToFirst()){
            Log.i(TAG,"Device already exists: "+c.getString(0)+", "+c.getString(1)+", "+c.getString(2)+", "+c.getString(3));
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

    public int updateDevice(int id, String updatedName, String updatedRoom, String updatedType, String updatedMac){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "
                +DevicesDB.COLUMN_ID +"="+id,null);
        if(!c.moveToFirst()){
            Log.i(TAG,"No such device: "+id+", can not change to "+updatedName.trim()+", "+updatedRoom.trim()+", "+updatedType.trim()+", "+updatedMac);
            //dumpDBtoLog();
            return -1;
        }
        else{
            ContentValues values= new ContentValues();
            values.put(DevicesDB.COLUMN_NAME, updatedName.trim());
            values.put(DevicesDB.COLUMN_ROOM, updatedRoom.trim());
            values.put(DevicesDB.COLUMN_TYPE, updatedType.trim());
            values.put(DevicesDB.COLUMN_MAC, updatedMac.trim());
            db.update(DevicesDB.TABLE_NAME, values, DevicesDB.COLUMN_ID + "=" + id, null);
            return 0;
        }
    }
    public void emptyDB(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
        db.close();
    }
    public void deleteDevice(String name){
        SQLiteDatabase db=this.getWritableDatabase();
        try{
            db.delete(DevicesDB.TABLE_NAME, DevicesDB.COLUMN_NAME + "=" + "'" + name.trim() + "'", null);
        }
        catch(SQLiteException e){
            Log.i(TAG,"No columns available to delete");
        }
        //dumpDBtoLog();
        db.close();
    }

    public int getIDSpecificDevice(String name, String room, String type, String mac){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "
                +DevicesDB.COLUMN_NAME +"="+"'"+name.trim()+"'"+" AND "
                +DevicesDB.COLUMN_ROOM +"="+"'"+room.trim()+"'"+" AND "
                +DevicesDB.COLUMN_TYPE +"="+"'"+type.trim()+"'"+" AND "
                +DevicesDB.COLUMN_MAC +"="+"'"+mac.trim()+"'",null);

        if(!c.moveToFirst()){
            Log.i(TAG,"No such device exists: "+name.trim()+", "+room.trim()+", "+type.trim());
            //dumpDBtoLog();
            return -1;
        }
        else{
            return Integer.parseInt(c.getString(0));
        }
    }

    //Allows multiple devices with the same name (can differentiate in terms of room)
    public List<Integer> getIDDevices(String name){
        SQLiteDatabase db=this.getReadableDatabase();
        List<Integer> results=new ArrayList<Integer>();
        Cursor c=db.rawQuery("SELECT * FROM "+DevicesDB.TABLE_NAME+" WHERE "+DevicesDB.COLUMN_NAME+"="+"'"+name.trim()+"'",null);
        if(c.moveToFirst()==false){
            Log.i(TAG, "No devices with name: "+name.trim());
            results.add(-1);
            return results;
        }
        int rowCount=0;
        do{
            rowCount++;
            results.add(Integer.parseInt(c.getString(0)));
        }
        while(c.moveToNext());

        if(rowCount>1){
            Log.i(TAG,"Multiple devices with this name");
        }
        return results;
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
