package com.iotmanager;

import android.provider.BaseColumns;

/**
 * Created by connorstein on 15-07-17.
 * Table and column names for the database
 *
 */
public final class DevicesDBContract {

    public DevicesDBContract(){}

    public static abstract class DevicesDB implements BaseColumns{
        public static final String TABLE_NAME="Devices";
        public static final String COLUMN_ID="ID";
        public static final String COLUMN_NAME="NAME";
        public static final String COLUMN_ROOM="ROOM";
        public static final String COLUMN_TYPE="TYPE";
        public static final String COLUMN_MAC="MAC";
    }


}
