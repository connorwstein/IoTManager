package com.iotmanager;

import android.test.AndroidTestCase;

import junit.framework.TestCase;

/**
 * Created by connorstein on 15-07-17.
 */
public class DeviceDBTest extends AndroidTestCase {

    DeviceDBHelper deviceDBHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deviceDBHelper=new DeviceDBHelper(getContext());
    }

    public void testDB(){
        deviceDBHelper.addDevice("MyDev", "Kitchen", "temp");
        deviceDBHelper.dumpDBtoLog();
        deviceDBHelper.deleteDevice("MyDev");
        deviceDBHelper.dumpDBtoLog();
    }


    @Override
    public void tearDown() throws Exception{
        super.tearDown();
    }
}
