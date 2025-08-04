package com.ultimafurniture.lynx;

import android.app.Activity;

import com.ultimafurniture.lynx.bean.MonitorDataBean;
import com.ultimafurniture.lynx.bean.type.Key;
import com.ultimafurniture.lynx.bean.type.LinkType;
import com.ultimafurniture.lynx.util.ReaderHelper;
import com.ultimafurniture.lynx.util.ActivityLifecycleUtils;
import com.ultimafurniture.lynx.util.XLog;
import com.orhanobut.hawk.Hawk;
import com.payne.reader.Reader;
import com.payne.reader.bean.receive.Version;
import com.payne.reader.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Desc：
 * <p>
 * <p>
 * Create on 2023-03-21
 */
public class GlobalCfg {
    private static final GlobalCfg sI = new GlobalCfg();
    /**
     * Default max is 33, E710 max is 36
     */
    private int mMaxOutPower = 33;
    /**
     * Link Type
     */
    private LinkType mLinkType;
    /**
     * Version
     */
    private volatile Version mVersion;

    /**
     * Enable Monitor
     */
    private volatile boolean mIsEnableMonitor;
    /**
     * Epc List
     */
    private final ArrayList<String> mEpcList = new ArrayList<>();
    /**
     * Monitor data, max 100
     */
    private final ConcurrentLinkedDeque<MonitorDataBean> mMonitorDataBeanList = new ConcurrentLinkedDeque<>();
    private ArrayList<MonitorDataBean> mList = new ArrayList<>();

    private GlobalCfg() {
    }

    public static GlobalCfg get() {
        return sI;
    }

    public void init() {
        mIsEnableMonitor = Hawk.get(Key.ENABLE_MONITOR, false);

        Reader reader = ReaderHelper.getReader();
        reader.setOriginalDataCallback(bytes -> {
            String str = null;
            if (XLog.sShowLog) {
                str = ArrayUtils.bytesToHexString(bytes, 0, bytes.length);
                XLog.d("--Send: " + str);
            }
            if (mIsEnableMonitor) {
                if (str == null) {
                    str = ArrayUtils.bytesToHexString(bytes, 0, bytes.length);
                }
                MonitorDataBean bean = new MonitorDataBean(str, true);
                addMonitorData(bean);
            }
        }, bytes -> {
            String str = null;
            if (XLog.sShowLog) {
                str = ArrayUtils.bytesToHexString(bytes, 0, bytes.length);
                XLog.i("--Recv: " + str);
            }
            if (mIsEnableMonitor) {
                if (str == null) {
                    str = ArrayUtils.bytesToHexString(bytes, 0, bytes.length);
                }
                MonitorDataBean bean = new MonitorDataBean(str, false);
                addMonitorData(bean);
            }
        });
    }

    public void setVersion(Version version) {
        if (version == null) {
            version = new Version();
            version.setVersion("0.0");
            version.setChipType(Version.ChipType.R2000);
        }
        mVersion = version;
        Version.ChipType chipType = version.getChipType();
        switch (chipType) {
            case E710: {
                mMaxOutPower = 36;
            }
                break;
        }
    }

    public Version getVersion() {
        return mVersion;
    }

    public int getMaxOutPower() {
        return mMaxOutPower;
    }

    public LinkType getLinkType() {
        return mLinkType;
    }

    public void setLinkType(LinkType linkType) {
        mLinkType = linkType;
    }

    public ArrayList<String> getNewEpcList() {
        return new ArrayList<>(mEpcList);
    }

    public void addEpc(String epc) {
        if (mEpcList.contains(epc)) {
            return;
        }
        mEpcList.add(epc);
    }

    public void clearEpcList() {
        mEpcList.clear();
    }

    public List<MonitorDataBean> getMonitorData() {
        mList.clear();
        if (mMonitorDataBeanList.size() > 0) {
            MonitorDataBean[] beans = new MonitorDataBean[0];
            MonitorDataBean[] array = mMonitorDataBeanList.toArray(beans);
            List<MonitorDataBean> asList = Arrays.asList(array);
            mList.addAll(asList);
        }
        return mList;
    }

    public boolean isEnableMonitor() {
        return mIsEnableMonitor;
    }

    public void saveMonitorStatus(boolean isEnableMonitor) {
        this.mIsEnableMonitor = isEnableMonitor;
        Hawk.put(Key.ENABLE_MONITOR, isEnableMonitor);
    }

    public void addMonitorData(MonitorDataBean bean) {
        if (bean == null || !mIsEnableMonitor) {
            return;
        }
        boolean isOverCapacity = mMonitorDataBeanList.size() >= 100;
        if (isOverCapacity) {
            mMonitorDataBeanList.removeLast();
        }
        mMonitorDataBeanList.addFirst(bean);
        Activity topActivity = ActivityLifecycleUtils.getTopActivity();
        if (topActivity == null || topActivity.isFinishing() || topActivity.isDestroyed()) {
            return;
        }


    }

    public void clearMonitorData() {
        mMonitorDataBeanList.clear();
    }
}
