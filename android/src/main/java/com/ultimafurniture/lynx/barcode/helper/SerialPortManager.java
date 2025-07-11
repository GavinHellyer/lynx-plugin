package com.ultimafurniture.lynx.barcode.helper;

import com.nativec.tools.SerialPort;

import java.io.File;
import java.io.IOException;

/**
 * Created by lei.li on 2016-12-28.
 */

public class SerialPortManager {

    private static final String DEV_FILE = "dev/ttyS3";
    private static final int BAUDRATE = 115200;
    private static final int RW_MODE = 0;
    private static SerialPort mSerialPort = null;

    static {
        try {
            mSerialPort = new SerialPort(new File(DEV_FILE), BAUDRATE, RW_MODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the serial port instance.
     * 
     * @return SerialPort the object of serial port.
     */
    public static SerialPort getSerialPort() {
        return mSerialPort;
    }

    /**
     * Close the serial port.
     */
    public static void close() {
        if (mSerialPort != null) {
            mSerialPort.close();
        }
    }
}
