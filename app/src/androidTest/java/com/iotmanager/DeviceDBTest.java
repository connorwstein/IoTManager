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
        Device testDev=new Device("My Dev",null,"ABCD","Kitchen","Temperature");
        deviceDBHelper.addDevice(testDev);
        assertEquals(deviceDBHelper.getID(testDev) == -1, false); //ensure non-negative id
        //Try to add a device that already exists
        assertEquals(deviceDBHelper.addDevice(testDev), -1);
        //Update device
        int id=deviceDBHelper.getID(testDev);
        testDev.setName("UpdatedDev");
        testDev.setRoom("Living");
        testDev.setType("Camera");
        testDev.setMac("EFGH");
        assertEquals(deviceDBHelper.updateDevice(id, testDev) == 0, true);
        assertEquals(deviceDBHelper.getID(new Device("My Dev",null,"ABCD","Kitchen","Temperature"))==-1,true); //ensure old device no longer exists
        //Test deleting the device
        deviceDBHelper.deleteDevice(testDev);
        assertEquals(deviceDBHelper.getID(testDev) == -1, true); //Make sure its gone
        deviceDBHelper.dumpDBtoLog();
    }

    @Override
    public void tearDown() throws Exception{
        super.tearDown();
    }
}
