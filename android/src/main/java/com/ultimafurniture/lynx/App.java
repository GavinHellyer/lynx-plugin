package com.ultimafurniture.lynx;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.naz.serial.port.BuildConfig;
import com.ultimafurniture.lynx.acts.SerialPortActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.naz.serial.port.SerialPortFinder;
import com.naz.serial.port.ModuleManager;
import com.orhanobut.hawk.Hawk;
import com.payne.connect.port.SerialPortHandle;
import com.payne.reader.Reader;
import com.payne.reader.bean.receive.Version;
import com.payne.reader.bean.config.Cmd;
import com.payne.reader.bean.config.ResultCode;
import com.payne.reader.base.Consumer;
import com.ultimafurniture.lynx.bean.type.Key;
import com.ultimafurniture.lynx.bean.type.LinkType;
import com.ultimafurniture.lynx.util.BeeperHelper;
import com.ultimafurniture.lynx.util.ReaderHelper;
import com.ultimafurniture.lynx.util.RxBleHelper;
import com.ultimafurniture.lynx.util.ActivityLifecycleUtils;
import com.ultimafurniture.lynx.util.PowerUtils;
import com.ultimafurniture.lynx.util.ReaderHelper;
import com.ultimafurniture.lynx.util.XLog;

import io.reactivex.disposables.Disposable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import com.ultimafurniture.lynx.bean.t30.DevicePower;
import com.payne.reader.communication.ConnectHandle;
import android.os.Message;

/**
 * Unified main application class containing:
 * - App Application entry point
 * - SerialPort Activity singleton
 * - Home Activity singleton
 */
public class App extends Application {

    SerialPortActivity serialPortActivity;

    @Override
    public void onCreate() {
        long startTime = System.currentTimeMillis();
        super.onCreate();

        Hawk.init(this).build();
        XLog.init(this, true /*debug*/, false);
        ErrorReport.init();
        ActivityLifecycleUtils.init(this);
        GlobalCfg.get().init();

        BeeperHelper.init(this);

        serialPortActivity = SerialPortActivity.getInstance();
        serialPortActivity.init(getApplicationContext());

        XLog.i("App initialization took " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        PowerUtils.powerOff();
        ReaderHelper.getReader().disconnect();
        RxBleHelper.release();
        BeeperHelper.release();
        ModuleManager.newInstance().release();

        ActivityLifecycleUtils.finishAll();
        System.exit(0);
    }

    /**
     * SerialPortActivity singleton manages serial port connection lifecycle
     */
    public static class SerialPortActivity {
        private static volatile SerialPortActivity INSTANCE;

        private Context context;
        public HomeActivity homeActivity;

        private ConnectHandle mConnectHandle;
        private boolean mPowerOn;
        private String mDevicePath;
        private int mBaudRate;

        private SerialPortActivity() {
            if (INSTANCE != null) throw new RuntimeException("Use getInstance()");
        }

        public static SerialPortActivity getInstance() {
            if (INSTANCE == null) {
                synchronized (SerialPortActivity.class) {
                    if (INSTANCE == null) INSTANCE = new SerialPortActivity();
                }
            }
            return INSTANCE;
        }

        public void init(Context context) {
            this.context = context;
            SerialPortFinder portFinder = new SerialPortFinder();
            String[] devicesPath = portFinder.getAllDevicesPath();

            ArrayList<String> deviceList = new ArrayList<>();
            if (devicesPath == null || devicesPath.length == 0) {
                deviceList.addAll(Arrays.asList("/dev/ttyS0","/dev/ttyS1","/dev/ttyS2","/dev/ttyS3","/dev/ttyS4"));
            } else {
                deviceList.addAll(Arrays.asList(devicesPath));
            }

            String preferredPath = "ttyS4";
            String normalDevice = "ttyAS3";

            for (String device : deviceList) {
                if (device.contains(preferredPath) || device.contains(normalDevice)) {
                    mDevicePath = device;
                    break;
                }
            }

            mBaudRate = Integer.parseInt(Hawk.get(Key.KEY_BAUD_RATE, "115200"));

            toConnect(true);

            homeActivity = HomeActivity.getInstance();
            homeActivity.init(context);
        }

        public void toConnect(boolean needCheck) {
            mPowerOn = PowerUtils.powerOn();
            XLog.i("PowerOn: " + mPowerOn);

            XLog.i("Connecting: " + mDevicePath + ", BaudRate: " + mBaudRate);

            mConnectHandle = new SerialPortHandle(mDevicePath, mBaudRate);
            boolean connected = ReaderHelper.getReader().connect(mConnectHandle);

            XLog.i("Connected: " + connected);
            XLog.i("Reader isConnected: " + ReaderHelper.getReader().isConnected());

            GlobalCfg.get().setLinkType(LinkType.LINK_TYPE_SERIAL_PORT);

            if (!needCheck) {
                Version version = new Version();
                version.setVersion("...");
                version.setChipType(Version.ChipType.E710);
                GlobalCfg.get().setVersion(version);
            }

            Hawk.put(Key.KEY_LINK_TYPE, LinkType.LINK_TYPE_SERIAL_PORT);
            Hawk.put(Key.KEY_DEVICE_PATH, mDevicePath);
            Hawk.put(Key.KEY_BAUD_RATE, "115200");
        }
    }


    /**
     * HomeActivity singleton manages UI-less app logic such as power and battery
     */
    public static class HomeActivity {
        private static volatile HomeActivity INSTANCE;

        public static HomeActivity getInstance() {
            if (INSTANCE == null) {
                synchronized (HomeActivity.class) {
                    if (INSTANCE == null) INSTANCE = new HomeActivity();
                }
            }
            return INSTANCE;
        }

        private Context context;
        private Disposable disposable;

        private volatile int sCurrLevel;
        private BroadcastReceiver mBatteryReceiver;

        private volatile boolean mIsSleep = false;
        private volatile boolean mIsRecharge = false;
        private boolean mAllowShow = true;
        private AlertDialog mAlertDialog;

        private HomeActivity() {
            if (INSTANCE != null) throw new RuntimeException("Use getInstance()");
        }

        // Handler message IDs
        private static final int MSG_REFRESH_UI = 0;
        private static final int MSG_REFRESH_POWER = 1;
        private static final int VERSION_SUCCESS = 2;
        private static final int POWER_SUCCESS = 3;

        // @SuppressLint("HandlerLeak")
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MSG_REFRESH_UI:
                        // UI refresh placeholder
                        break;
                    case MSG_REFRESH_POWER:
                        refreshPower();
                        break;
                    case VERSION_SUCCESS:
                        Version version = (Version) msg.obj;
                        ReaderHelper.getReader().removeCallback(Cmd.GET_FIRMWARE_VERSION);
                        GlobalCfg.get().setVersion(version);
                        break;
                    case POWER_SUCCESS:
                        // Power state processing placeholder
                        DevicePower devicePower = (DevicePower) msg.obj;
                        int v = devicePower.getDevicePower();
                        int powerLevel = (v - 3500) / 65;
                        break;
                }
            }
        };

        public void init(Context context) {
            this.context = context;
            registerScreenReceiver();
            registerBatteryReceiver();
            setupReader();
        }

        private void setupReader() {
            Reader reader = ReaderHelper.getReader();
            if (reader == null || !reader.isConnected()) {
                XLog.w("Reader not connected, skipping init");
                return;
            }
            BeeperHelper.beep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);

            reader.setCommandStatusCallback(cmdStatus -> {
                if (cmdStatus.getStatus() == ResultCode.REQUEST_TIMEOUT) return;
                String status = String.format("Cmd: %s, Status: %d",
                        Cmd.getNameForCmd(cmdStatus.getCmd()), cmdStatus.getStatus());
                XLog.i(status);
            });
        }

        private void registerScreenReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            context.registerReceiver(mScreenReceiver, filter);
        }

        private void registerBatteryReceiver() {
            if (mBatteryReceiver != null) return;
            if (GlobalCfg.get().getLinkType() != LinkType.LINK_TYPE_SERIAL_PORT) return;

            mBatteryReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    sCurrLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                }
            };
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(mBatteryReceiver, filter);
        }

        private void refreshPower() {
            ReaderHelper.getDefaultHelper().getDevicePower(devicePower -> {
                // handle device power
                mHandler.sendEmptyMessageDelayed(MSG_REFRESH_POWER, 14000);
            }, failure -> {
                // handle failure
            });
        }

        private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    mIsSleep = false;
                    if (GlobalCfg.get().getLinkType() == LinkType.LINK_TYPE_BLUETOOTH) {
                        ReaderHelper.getDefaultHelper().wakeupInterfaceBoard();
                        mHandler.removeMessages(MSG_REFRESH_POWER);
                        mHandler.sendEmptyMessage(MSG_REFRESH_POWER);
                    }
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    mIsSleep = true;
                    if (GlobalCfg.get().getLinkType() == LinkType.LINK_TYPE_BLUETOOTH) {
                        mHandler.removeMessages(MSG_REFRESH_POWER);
                    }
                }
            }
        };

        public static int getBatteryLevel() {
            HomeActivity instance = getInstance();
            return instance == null ? 100 : instance.sCurrLevel;
        }
    }
}
