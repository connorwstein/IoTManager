package com.iotmanager;

import android.test.AndroidTestCase;
import android.util.Log;

import junit.framework.TestCase;

/**
 * Created by connorstein on 15-07-17.
 */

//extend TestCase if you do not need android stuff like context etc
//otherwise extend AndroidTestCase
public class DeviceDBTest extends AndroidTestCase {
    private static final String TAG="Connors Debug";
    DeviceDBHelper deviceDBHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deviceDBHelper=new DeviceDBHelper(getContext());
    }

    public void testDatabase(){
        deviceDBHelper.emptyDB();
        //Add a device then make sure its in the database by checking the ID, test with trailing white space (database should trim it)
        deviceDBHelper.addDevice("MyDev ", "Kitchen ", "Temperature");
        assertEquals(deviceDBHelper.getIDDevices("MyDev").get(0) == -1, false); //ensure non-negative id
        assertEquals(deviceDBHelper.getIDSpecificDevice("MyDev", "Kitchen", "Temperature")==-1,false);
        //Try to add a device that already exists
        assertEquals(deviceDBHelper.addDevice("MyDev", "Kitchen", "Temperature"), -1);
        //Update device
        int id=deviceDBHelper.getIDSpecificDevice("MyDev","Kitchen","Temperature");
        assertEquals(deviceDBHelper.updateDevice(id, " MyUpdatedDev", "Living Room ", "Camera")==0,true);
        assertEquals(deviceDBHelper.getIDDevices("MyDev").get(0)==-1,true);
        assertEquals(deviceDBHelper.getIDSpecificDevice("MyDev","Kitchen","Temperature")==-1,true); //should no longer be this device
        //Test deleting the device
        deviceDBHelper.deleteDevice("MyUpdatedDev");
        assertEquals(deviceDBHelper.getIDDevices("MyUpdatedDev").get(0) == -1, true); //Make sure its gone
        assertEquals(deviceDBHelper.getIDSpecificDevice("MyUpdatedDev","Living Room","Camera")==-1,true);
        deviceDBHelper.dumpDBtoLog();
    }

    @Override
    public void tearDown() throws Exception{
        super.tearDown();
    }
}
