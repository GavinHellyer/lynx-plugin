package com.ultimafurniture.lynx.acts;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.naz.serial.port.SerialPortFinder;
import com.orhanobut.hawk.Hawk;
import com.payne.connect.port.SerialPortHandle;
import com.payne.reader.bean.receive.Version;
import com.payne.reader.communication.ConnectHandle;
import com.ultimafurniture.lynx.GlobalCfg;
import com.ultimafurniture.lynx.RFIDReaderListener;
import com.ultimafurniture.lynx.bean.type.Key;
import com.ultimafurniture.lynx.bean.type.LinkType;
import com.ultimafurniture.lynx.util.PowerUtils;
import com.ultimafurniture.lynx.util.ReaderHelper;
import com.ultimafurniture.lynx.util.XLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class SerialPortActivity {

    Context context;

   private static volatile SerialPortActivity INSTANCE = null;
  public  HomeActivity homeActivity;
    private ConnectHandle mConnectHandle;
    private boolean mPowerOn;
    private String mDevicePath;
    private int mBaudRate;

    private InnerHandler mH = new InnerHandler(this);

    // Private constructor prevents instantiation
    private SerialPortActivity() {
        // Optional: protect against reflection
        if (INSTANCE != null) {
            throw new RuntimeException("Use getInstance() method");
        }
    }



    public static SerialPortActivity getInstance() {
        if (INSTANCE == null) {
            synchronized (SerialPortActivity.class) {
                // Double-check inside synchronized block
                if (INSTANCE == null) {
                    INSTANCE = new SerialPortActivity();
                }
            }
        }
        return INSTANCE;
    }

    private static class InnerHandler extends Handler {
        private final int MSG_CONNECT = 0;
        private WeakReference<SerialPortActivity> mWr;

        InnerHandler(SerialPortActivity a) {
            mWr = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            SerialPortActivity a = mWr.get();
            if (a == null) {
                return;
            }
            switch (msg.what) {
                case MSG_CONNECT:
                    a.toConnect(true);
                    break;
                default:
                    break;
            }
        }
    }


    public void init(Context context) {
        SerialPortFinder portFinder = new SerialPortFinder();
        String[] devicesPath = portFinder.getAllDevicesPath();

        ArrayList<String> list = new ArrayList<>();
        if (devicesPath == null || devicesPath.length == 0) {
            list.add("/dev/ttyS0");
            list.add("/dev/ttyS1");
            list.add("/dev/ttyS2");
            list.add("/dev/ttyS3");
            list.add("/dev/ttyS4");
        } else {
            list.addAll(Arrays.asList(devicesPath));
        }


        String lastDevicePath = Hawk.get(Key.KEY_DEVICE_PATH, "ttyS4");
        String normalDevice = "ttyAS3";
        int length = list.size();

        String baudRateStr = Hawk.get(Key.KEY_BAUD_RATE, "115200");

        toConnect(true);

        homeActivity =  HomeActivity.getInstance();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Your code to run after delay
                homeActivity.init(context);
            }
        }, 2000);


    }

    public void toConnect(boolean needCheck) {
        mPowerOn = PowerUtils.powerOn();
        XLog.i("powerOn:" + mPowerOn);

        mDevicePath = "/dev/ttyS4";
        mBaudRate = Integer.parseInt("115200");


        XLog.i("To Connect: " + "/dev/ttyS4" + ", " + mBaudRate);

        mConnectHandle = new SerialPortHandle(mDevicePath, mBaudRate);
        boolean connectSuccess = ReaderHelper.getReader().connect(mConnectHandle);
        XLog.i("connectSuccess: " + connectSuccess);
        XLog.i("isConnected: " + ReaderHelper.getReader().isConnected());
        GlobalCfg globalCfg = GlobalCfg.get();
        globalCfg.setLinkType(LinkType.LINK_TYPE_SERIAL_PORT);
        if (!needCheck) {
            Version version = new Version();
            version.setVersion("...");
            version.setChipType(Version.ChipType.E710);
            globalCfg.setVersion(version);
        }

        Hawk.put(Key.KEY_LINK_TYPE, LinkType.LINK_TYPE_SERIAL_PORT);
        Hawk.put(Key.KEY_DEVICE_PATH, mDevicePath);
        Hawk.put(Key.KEY_BAUD_RATE, "115200");
    }


}