package com.iotmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.iotmanager.DevicesDBContract.DevicesDB;
/**
 * Created by connorstein on 15-07-17.
 */
public class DeviceDBHelper extends SQLiteOpenHelper {
    private static final String TAG="Connors Debug";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + DevicesDB.TABLE_NAME + " (" +
                    DevicesDB.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    DevicesDB.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    DevicesDB.COLUMN_ROOM + TEXT_TYPE + COMMA_SEP +
                    DevicesDB.COLUMN_TYPE + TEXT_TYPE +
            " )";

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

    public void addDevice(String name, String room, String type){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(DevicesDB.COLUMN_NAME, name);
        values.put(DevicesDB.COLUMN_ROOM, room);
        values.put(DevicesDB.COLUMN_TYPE, type);
        db.insert(DevicesDB.TABLE_NAME,null,values);
        db.close();

    }
    public void deleteDevice(String name){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(DevicesDB.TABLE_NAME,DevicesDB.COLUMN_NAME+"="+name,null);
        db.close();
    }
    public void dumpDBtoLog(){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM " + DevicesDB.TABLE_NAME + ";", null);
        c.moveToFirst();
        if(c!=null){
            Log.i(TAG, "Device Database: ");
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
