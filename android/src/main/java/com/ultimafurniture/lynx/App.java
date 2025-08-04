package com.ultimafurniture.lynx;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.naz.serial.port.BuildConfig;
import com.ultimafurniture.lynx.acts.SerialPortActivity;
import com.ultimafurniture.lynx.util.BeeperHelper;
import com.ultimafurniture.lynx.util.ReaderHelper;
import com.ultimafurniture.lynx.util.RxBleHelper;
import com.ultimafurniture.lynx.util.ActivityLifecycleUtils;
import com.ultimafurniture.lynx.util.PowerUtils;
import com.ultimafurniture.lynx.util.XLog;
import com.naz.serial.port.ModuleManager;
import com.orhanobut.hawk.Hawk;

/**
 * @author naz
 *         Date 2019/11/20
 */
public class App extends Application {

    SerialPortActivity serialPortActivity;

    @Override
    public void onCreate() {
        long l = System.currentTimeMillis();
        super.onCreate();
        Hawk.init(this).build();
        XLog.init(this, BuildConfig.DEBUG, false);
        ErrorReport.init();
        ActivityLifecycleUtils.init(this);
        GlobalCfg.get().init();
        XLog.i("App.onCreate()");
        BeeperHelper.init(this);
        serialPortActivity =   SerialPortActivity.getInstance();
        serialPortActivity.init(getApplicationContext());

        XLog.i("hs." + (System.currentTimeMillis() - l));
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
        // android.os.Process.killProcess(android.os.Process.myPid());
    }
}