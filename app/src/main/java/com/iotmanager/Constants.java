package com.iotmanager;

/**
 * Created by connorstein on 15-06-03.
 * All commands for communication with the ESP devices
 */
public final class Constants {
    //Broadcasts
    public static final String HELLO_DEVICES="Hello ESP Devices?";
    public static final String LOCATION_MODE="Devices Low Power";
    public static final String DEFAULT_DEVICE_BROADCAST_IP="255.255.255.255";

    //Ports
    public static final int DEFAULT_DEVICE_TCP_PORT=80;
    public static final int DEFAULT_DEVICE_UDP_PORT=1025;

    //IPs
    public static final String DEFAULT_DEVICE_IP="192.168.4.1";

    //Generic Commands
    public static final String COMMAND_CONNECT="Connect:";
    public static final String COMMAND_MAC_GET="Mac Get";
    public static final String COMMAND_RUN_AP="Run AP";
    public static final String RESPONSE_FAIL="Failed";

    //Naming
    public static final String COMMAND_NAME="Name:";
    public static final String RESPONSE_NAME_SUCCESS="Name Set";

    //Setting type
    public static final String COMMAND_TYPE="Type:";
    public static final String RESPONSE_TYPE_SUCCESS="Type Set";

    //Setting room
    public static final String COMMAND_ROOM="Room:";
    public static final String RESPONSE_ROOM_SUCCESS="Room Set";

    //Temperaure sensor
    public static final String COMMAND_TEMPERATURE_GET="Temperature Get";

    //Lighting
    public static final String COMMAND_LIGHTING_SET="Lighting Set:";
    public static final String COMMAND_LIGHTING_GET="Lighting Get";

    //Camera take picture
    public static final String COMMAND_CAMERA_TAKE_PICTURE="Camera Take Picture";
    public static final String RESPONSE_TAKE_PICTURE_SUCCESS="Picture Taken";
    public static final String RESPONSE_TAKE_PICTURE_FAIL="Picture Take Fail";

    //Camera compression ratio
    public static final String COMMAND_CAMERA_CHANGE_COMPRESSION="Camera Compression Ratio:";
    public static final String RESPONSE_CAMERA_CHANGE_COMPRESSION_SUCCESS="Camera Compression Ratio Set";
    public static final String RESPONSE_CAMERA_CHANGE_COMPRESSION_FAIL="Camera Compression Ratio Fail";

    //Camera image size
    public static final String COMMAND_CAMERA_CHANGE_IMAGE_SIZE="Camera Image Size Set:";
    public static final String RESPONSE_CAMERA_CHANGE_IMAGE_SIZE_SUCCESS="Camera Image Size Set";
    public static final String RESPONSE_CAMERA_CHANGE_IMAGE_SIZE_FAIL="Camera Image Size Set Fail";

    //Camera get picture
    public static final String COMMAND_CAMERA_GET_PICTURE="Camera Get Picture";

    //Camera stop pciture
    public static final String COMMAND_CAMERA_STOP_PICTURE="Camera Stop Picture";

    //Camera get size
    public static final String COMMAND_CAMERA_GET_SIZE="Camera Get Size";

    //Heater on off
    public static final String COMMAND_HEATER_SET_ON="Heater On";
    public static final String COMMAND_HEATER_SET_OFF="Heater Off";

}
