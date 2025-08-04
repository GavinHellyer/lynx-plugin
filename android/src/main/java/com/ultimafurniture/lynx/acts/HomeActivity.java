package com.ultimafurniture.lynx.acts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.orhanobut.hawk.Hawk;
import com.payne.reader.Reader;
import com.payne.reader.base.Consumer;
import com.payne.reader.bean.config.Cmd;
import com.payne.reader.bean.config.CmdStatus;
import com.payne.reader.bean.config.ResultCode;
import com.payne.reader.bean.receive.Failure;
import com.payne.reader.bean.receive.Version;
import com.ultimafurniture.lynx.GlobalCfg;
import com.ultimafurniture.lynx.RFIDReaderListener;
import com.ultimafurniture.lynx.bean.LogBean;
import com.ultimafurniture.lynx.bean.t30.DevicePower;
import com.ultimafurniture.lynx.bean.t30.TriggerKey;
import com.ultimafurniture.lynx.bean.type.Key;
import com.ultimafurniture.lynx.bean.type.LinkType;
import com.ultimafurniture.lynx.fragments.home.inventory.InventoryTagFragment;
import com.ultimafurniture.lynx.util.BeeperHelper;
import com.ultimafurniture.lynx.util.ReaderHelper;
import com.ultimafurniture.lynx.util.StringFormat;
import com.ultimafurniture.lynx.util.ToastUtils;
import com.ultimafurniture.lynx.util.XLog;

import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * @author naz
 */
public class HomeActivity {
    private static volatile HomeActivity INSTANCE = null;
  public  InventoryTagFragment inventoryTagFragment;
    private final int MSG_REFRESH_UI = 0;
    private final int MSG_REFRESH_POWER = 1;

    private final int VERSION_SUCCESS = 2;

    private final int POWER_SUCCESS = 3;

    public static HomeActivity sHomeActivity;
    private AlertDialog mExitDialog;


    private Context context;
    private Consumer<TriggerKey> mTriggerKeyConsumer;

    private Disposable mDisposable;

    private volatile int sCurrLevel;
    private BroadcastReceiver mBatteryReceiver;

    private volatile boolean mIsSleep;
    private volatile boolean mIsRecharge = false;
    private boolean mAllowShow = true;
    private AlertDialog mAlertDialog;

    private HomeActivity() {
        // Optional: protect against reflection
        if (INSTANCE != null) {
            throw new RuntimeException("Use getInstance() method");
        }
    }



    public static HomeActivity getInstance(){
        if (INSTANCE == null) {
            synchronized (HomeActivity.class) {
                // Double-check inside synchronized block
                if (INSTANCE == null) {
                    INSTANCE = new HomeActivity();
                }
            }
        }
        return INSTANCE;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI: {
                    // delayInitUI();
                }
                break;
                case MSG_REFRESH_POWER: {
                    refreshPower();
                }

                break;
                case VERSION_SUCCESS: {
                    Version version = (Version) msg.obj;
                    ReaderHelper.getReader().removeCallback(Cmd.GET_FIRMWARE_VERSION);
                    GlobalCfg.get().setVersion(version);
                }

                break;
                case POWER_SUCCESS: {
                    DevicePower devicePower = (DevicePower) msg.obj;
                    int v = devicePower.getDevicePower();
                    int powerLevel = (v - 3500) / 65;
                }
            }
        }
    };
    private Consumer<Version> successConsumer = version -> {

        Message m = Message.obtain();
        m.what = VERSION_SUCCESS;
        m.obj = version;
        mHandler.sendMessage(m);

    };
    private Consumer<DevicePower> powerConsumer = devicePower -> {

        Message m = Message.obtain();
        m.what = POWER_SUCCESS;
        m.obj = devicePower;
        mHandler.sendMessage(m);

    };
    private Consumer<Failure> failureConsumer = failure -> XLog
            .w(Cmd.getNameForCmd(failure.getCmd()) + ",获取失败:" + Failure.getNameForResultCode(failure.getErrorCode()));

    // <editor-fold desc="Top Btn mL">


    // </editor-fold>

    private void refreshPower() {
        ReaderHelper.getDefaultHelper().getDevicePower(powerConsumer, failureConsumer);
        mHandler.removeMessages(MSG_REFRESH_POWER);
        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_POWER, 14000);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, HomeActivity.class);
        context.startActivity(starter);
    }

    public static int getBatteryLevel() {
        if (sHomeActivity == null) {
            return 100;
        }
        return sHomeActivity.sCurrLevel;
    }



    public void init(Context context) {
        this.context = context;

        registScreenReceiver();
        registBatteryReceiver();
        setupReader();

        inventoryTagFragment =  InventoryTagFragment.getInstance();


        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                inventoryTagFragment.init(context);
                refreshPower();

            }
        }, 2000);



    }

    private void setupReader() {
        Reader reader = ReaderHelper.getReader();
        if (reader == null || !reader.isConnected()) {
            XLog.w("stop init cfg.");
            return;
        }
//        reader.getFirmwareVersion(successConsumer, failureConsumer);
        BeeperHelper.beep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
        BeeperHelper.setup();


        long cmdTimeout = Hawk.get(Key.CMD_TIMEOUT, 6000L);/* 总的指令超时时间 */
        reader.setCmdTimeout(cmdTimeout);/* 进来时配置超时 */


        Consumer<CmdStatus> commandStatusCallback = cmdStatus -> {
            XLog.i("cmdStatus: " + cmdStatus.getStatus());
            XLog.i("getCmd: " + cmdStatus.getCmd());
            if (cmdStatus.getStatus() == ResultCode.REQUEST_TIMEOUT) {
                return;
            }
            String status = StringFormat.formatTempLabel2(cmdStatus.getCmd(), cmdStatus.getStatus());
            XLog.i("status : " + status);
            if (cmdStatus.getCmd() == Cmd.SET_WORK_ANTENNA) {
                status += ",Ant" + (reader.getCacheWorkAntenna() + 1);
            }
            if (cmdStatus.getStatus() == ResultCode.ANTENNA_MISSING_ERROR) {
                status += ",Ant" + (cmdStatus.getAntId() + 1);
            }
            LogBean bean = new LogBean(status, cmdStatus.getStatus() == ResultCode.SUCCESS);
        };
        reader.setCommandStatusCallback(commandStatusCallback);
    }


    /**
     * 获取存储权限
     */
    private void getStoragePermission() {
        boolean perOk;
        if (android.os.Build.VERSION.SDK_INT > 22) {
            perOk = (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
        } else {
            perOk = true;
        }
        if (perOk) {
//            showExportExcel();
            return;
        }

    }



    boolean mPressed;


    public boolean isSleep() {
        boolean isSleep = mIsSleep;
        // this.mIsSleep = false;
        return isSleep;
    }

    // 注册屏幕唤醒/锁屏广播
    private void registScreenReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(mScreenReceiver, filter);
    }


    public void registBatteryReceiver() {
        if (mBatteryReceiver != null) {
            XLog.i("Battery have registed...");
            return;
        }
        LinkType linkType = GlobalCfg.get().getLinkType();
        if (linkType != LinkType.LINK_TYPE_SERIAL_PORT) {
            XLog.i("No need regist battery receiver");
            return;
        }
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sCurrLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(mBatteryReceiver, filter);
    }

    private void unregisterBatteryReceiver() {
        if (mBatteryReceiver != null) {
            XLog.i("unregister...");
            try {
                context.unregisterReceiver(mBatteryReceiver);
                mBatteryReceiver = null;
            } catch (Exception e) {
                XLog.w(Log.getStackTraceString(e));
            }
        }
    }


    private void exit() {
        sHomeActivity = null;
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mHandler.removeCallbacksAndMessages(null);
//       context.unregisterForContextMenu(binding.appbar.btnMainExtra);
        try {
            context.unregisterReceiver(mScreenReceiver);
        } catch (Exception ignored) {
        }
        unregisterBatteryReceiver();
//      context.  getApplication().onTerminate();
    }


    private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LinkType linkType = GlobalCfg.get().getLinkType();
            String action = intent.getAction();

            if (Intent.ACTION_SCREEN_ON.equals(action)) {/* 亮屏 */
                mIsSleep = false;
                if (linkType == LinkType.LINK_TYPE_BLUETOOTH) {
                    ReaderHelper.getDefaultHelper().wakeupInterfaceBoard();
                    mHandler.removeMessages(MSG_REFRESH_POWER);
                    mHandler.sendEmptyMessage(MSG_REFRESH_POWER);
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {/* 息屏 */
                mIsSleep = true;
                if (linkType == LinkType.LINK_TYPE_BLUETOOTH) {
                    mHandler.removeMessages(MSG_REFRESH_POWER);
                }
            }
        }
    };
}