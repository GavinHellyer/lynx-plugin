package com.ultimafurniture.lynx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.payne.reader.bean.config.Beeper;
import com.ultimafurniture.lynx.GlobalCfg;
import com.ultimafurniture.lynx.RFIDReaderListener;
import com.ultimafurniture.lynx.bean.InventoryTagBean;
import com.ultimafurniture.lynx.bean.type.Key;
import com.ultimafurniture.lynx.util.BeeperHelper;
import com.ultimafurniture.lynx.util.ReaderHelper;
// import com.ultimafurniture.lynx.util.ToastUtils;
import com.ultimafurniture.lynx.util.XLog;
import com.orhanobut.hawk.Hawk;
import com.payne.reader.Reader;
import com.payne.reader.base.Consumer;
import com.payne.reader.bean.config.AntennaCount;
import com.payne.reader.bean.config.MemBank;
import com.payne.reader.bean.config.ResultCode;
import com.payne.reader.bean.receive.Failure;
import com.payne.reader.bean.receive.InventoryFailure;
import com.payne.reader.bean.receive.InventoryTag;
import com.payne.reader.bean.receive.InventoryTagEnd;
import com.payne.reader.bean.receive.OperationTag;
import com.payne.reader.bean.receive.OutputPower;
import com.payne.reader.bean.receive.ReaderTemperature;
import com.payne.reader.bean.receive.Success;
import com.payne.reader.bean.send.InventoryConfig;
import com.payne.reader.bean.send.InventoryParam;
import com.payne.reader.bean.send.ReadConfig;
import com.payne.reader.util.ArrayUtils;
import com.payne.reader.util.ThreadPool;
import com.getcapacitor.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capacitor RFID Inventory Plugin
 * Adapted from native fragment for Capacitor environment.
 */
public class InventoryTagPlugin extends Plugin {

    // Singleton instance
    private static volatile InventoryTagPlugin INSTANCE = null;

    /**
     * Provides singleton instance access
     */
    public static InventoryTagPlugin getInstance() {
        if (INSTANCE == null) {
            synchronized (InventoryTagPlugin.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InventoryTagPlugin();
                }
            }
        }
        return INSTANCE;
    }

    // Logger tag and timestamp formatter
    private final String TAG = "InventoryTagPlugin";
    private final SimpleDateFormat mSdf = XLog.getSafeDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    // Context and hardware objects
    private Context context;
    private Reader mReader;
    private InventoryParam mInventoryParam;

    // Data management structures
    private final LinkedList<InventoryTagBean> mData = new LinkedList<>();
    private final ConcurrentHashMap<String, InventoryTagBean> mMap = new ConcurrentHashMap<>(999);

    // State variables
    private volatile boolean mRunning = false;
    private boolean mFastSwitchAntMode;
    private boolean mFastMode;
    private boolean mReadTID;
    private boolean mSaveInventoryLog;

    private long mTotalCount = 0;
    private int mDelayMs = 10;

    private long mStartRunTime;
    private long mCmdStartTime;
    private long mSaveId;

    private byte mLowBatterySetPower;
    private int mBeepInterval;
    private long mLastBeepTime;

    private String mLogSavePath;
    private int mTempThreshold;
    private byte mReducePower;
    private int mAddDelayMs;
    private long mLastCheckTempMs;

    // Listeners
    private final List<RFIDReaderListener> listeners = new ArrayList<>();

    // Reader raw data consumer and message handler
    private InnerConsumer<byte[]> mConsumer;
    @SuppressLint("HandlerLeak")
    private final InnerHandler mHandler = new InnerHandler(this);

    // ------------------
    // Initialization
    // ------------------

    /**
     * Initialize plugin with context and reader setup
     */
    public void init(Context context) {
        this.context = context;
        mReader = ReaderHelper.getReader();
        mConsumer = new InnerConsumer<>();
        mReader.addOriginalDataReceivedCallback(mConsumer);
        mInventoryParam = new InventoryParam();
    }

    /**
     * Add or update listener for RFID scan events
     */
    public void setOnRFIDReaderListener(RFIDReaderListener listener) {
        listeners.remove(listener);
        listeners.add(listener);
    }

    /**
     * Get current scanned tags list
     */
    public List<InventoryTagBean> getTags() {
        return mData;
    }

    // ------------------
    // Inventory Control
    // ------------------

    /**
     * Start or stop inventory scanning
     * @param start true to start, false to stop
     */
    public boolean startStop(boolean start) {
        if (!start) {
            stopInventory();
            return true;
        }
        if (mRunning) {
            return true; // Already running
        }
        if (mInventoryParam.getInventory() == null) {
            Log.i(TAG, "Inventory parameter error");
            return false;
        }
        if (!mFastSwitchAntMode && !mInventoryParam.hasCheckedAnts()) {
            Log.i(TAG, "Please select at least one antenna");
            return false;
        }

        mRunning = true;

        // Battery power and beep config from Hawk storage
        mLowBatterySetPower = (byte) ((int) Hawk.get(Key.KEY_POWER, 28));
        mBeepInterval = Hawk.get(Key.MINI_NUM_BEEP_TIME, 0);
        mSaveInventoryLog = Hawk.get(Key.SAVE_INVENTORY_LOG, false);

        // Temperature threshold config
        try {
            String[] split = Hawk.get(Key.KEY_TEMP_THRESHOLD, "45,3,2000").split(",");
            mTempThreshold = Integer.parseInt(split[0]);
            mReducePower = (byte) Integer.parseInt(split[1]);
            mAddDelayMs = Integer.parseInt(split[2]);
        } catch (Exception ignored) {}

        mInventoryParam.setDelayMs(mDelayMs);

        // Inventory log saving setup if enabled
        if (mSaveInventoryLog) {
            mSaveId = 1;
            mLogSavePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "InventoryTesting-log.txt";
        }

        setInventoryConfig(mFastMode);
        mStartRunTime = System.currentTimeMillis();
        doNext(false);
        return true;
    }

    /**
     * Stop inventory scanning safely and clear state
     */
    private void stopInventory() {
        mConsumer.count = 0;
        mHandler.removeCallbacksAndMessages(null);
        if (mRunning && mReader != null) {
            mReader.stopInventory();
        }
        mRunning = false;
    }

    // ------------------
    // Inventory Processing
    // ------------------

    /**
     * Process next antenna inventory
     */
    private void doNext(boolean nextAntId) {
        if (!mRunning) return;
        int antennaId = mInventoryParam.getAntennaId(nextAntId);
        mReader.setWorkAntenna(antennaId, successConsumer, failureConsumer);
    }

    /**
     * Setup inventory reader configuration
     */
    private void setInventoryConfig(boolean isFast) {
        try {
            InventoryConfig config = new InventoryConfig.Builder()
                    .setInventoryParam(mInventoryParam)
                    .setInventory(mInventoryParam.getInventory())
                    .setOnInventoryTagSuccess(mInventoryTagSuccess)
                    .setOnInventoryTagEndSuccess(mInventoryTagEndSuccess)
                    .setOnFailure(this::onFailure)
                    .setFastInventory(isFast)
                    .build();
            mReader.setInventoryConfig(config);
        } catch (Exception e) {
            XLog.e(e.getMessage());
        }
    }

    /**
     * Failure callback handler
     */
    private void onFailure(InventoryFailure failure) {
        if (XLog.sShowLog) {
            XLog.w("Failure: ant=" + failure.getAntId()
                    + ",cmd=" + failure.getCmd()
                    + ",err=" + ResultCode.getNameForResultCode(failure.getErrorCode()));
        }
        if (!mFastSwitchAntMode) {
            doNext(true);
        }
    }

    // ------------------
    // Reader Consumers
    // ------------------

    private final Consumer<InventoryTag> mInventoryTagSuccess = inventoryTag -> {
        Message msg = Message.obtain();
        msg.what = mHandler.MSG_ON_RECV;
        msg.obj = inventoryTag;
        mHandler.sendMessage(msg);
    };

    private final Consumer<InventoryTagEnd> mInventoryTagEndSuccess = inventoryTagEnd -> {
        Message msg = Message.obtain();
        msg.what = mHandler.MSG_ON_END;
        msg.obj = inventoryTagEnd;
        mHandler.sendMessage(msg);
    };

    private final Consumer<OperationTag> mOnReadTagSuccess = ot -> {
        InventoryTag tag = new InventoryTag();
        tag.setPc(ot.getStrPc());
        tag.setEpc(ot.getStrEpc() + "\n" + ot.getStrData());
        tag.setAntId(ot.getAntId());
        tag.setRssi(ot.getRssi());
        tag.setFreq(ot.getFreq());
        mInventoryTagSuccess.accept(tag);

        if (ot.isEndTag()) {
            if (BeeperHelper.getBeeperType() == Beeper.ONCE_END_BEEP)
                toBeep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
            if (mDelayMs == 0)
                doNext(true);
            else
                mHandler.sendEmptyMessageDelayed(mHandler.MSG_RESUME, mDelayMs);
        } else {
            if (BeeperHelper.getBeeperType() == Beeper.PER_TAG_BEEP)
                toBeep(BeeperHelper.SOUND_FILE_TYPE_SHORT);
        }
    };

    private final Consumer<Failure> failureConsumer = failure -> {
        XLog.w("setAnt.Failure: " + ResultCode.getNameForResultCode(failure.getErrorCode()));
        doNext(false);
    };

    private final Consumer<Success> successConsumer = success -> {
        if (!mRunning) return;
        mCmdStartTime = System.currentTimeMillis();
        if (mReadTID) {
            ReadConfig config = new ReadConfig.Builder()
                    .setPasswords("00000000")
                    .setMemBankByte(MemBank.TID.getValue())
                    .setWordStartAddress((byte) 0)
                    .setWordLength((byte) 6)
                    .build();
            mReader.readTag(config, mOnReadTagSuccess, failureConsumer);
        } else {
            mReader.startInventory();
        }
    };

    // ------------------
    // Message Handling
    // ------------------

    private void onRecv(InventoryTag inventoryTag) {
        XLog.w("onRecv: " + inventoryTag);
        toBeep(BeeperHelper.SOUND_FILE_TYPE_SHORT);

        // Save EPC globally
        String epc = inventoryTag.getEpc();
        GlobalCfg.get().addEpc(epc);

        InventoryTagBean bean = mMap.get(epc);
        if (bean == null) {
            bean = new InventoryTagBean(inventoryTag, mData.size());
            mData.add(bean);
            mMap.put(epc, bean);
        } else {
            bean.setInventoryTag(inventoryTag);
            bean.addTimes();
        }

        mTotalCount++;

        // Notify listeners on the main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            for (RFIDReaderListener l : listeners) {
                l.onScanningStatus(true);
                l.onInventoryTag(inventoryTag);
            }
        });
    }

    private void onEnd(InventoryTagEnd inventoryTagEnd) {
        XLog.i("onEnd: " + inventoryTagEnd);

        boolean isGroupEnd = isGroupEnd(inventoryTagEnd);

        // Notify listeners on the main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            for (RFIDReaderListener l : listeners) {
                l.onInventoryTagEnd(inventoryTagEnd);
            }
        });

        if (!mRunning || inventoryTagEnd.isFinished()) {
            stopInventory();
            return;
        }

        if (isGroupEnd) {
            checkBattery();
            checkTemp();
        }
        if (!mFastSwitchAntMode) {
            if (mDelayMs == 0)
                doNext(true);
            else
                mHandler.sendEmptyMessageDelayed(mHandler.MSG_RESUME, mDelayMs);
        }
    }

    private boolean isGroupEnd(InventoryTagEnd end) {
        if (mFastSwitchAntMode) {
            if (mReader.getAntennaCount() == AntennaCount.SIXTEEN_CHANNELS)
                return end.getAntennaGroupId() == 1;
            else
                return true;
        }
        return mInventoryParam.isLastAnt();
    }

    // ------------------
    // Battery & Temperature Checking
    // ------------------

    private boolean checkBattery() {
        if (mLowBatterySetPower <= 0) return true;
        int level = com.ultimafurniture.lynx.acts.HomeActivity.getBatteryLevel();
        if (level > 0 && level < 11) {
            mReader.getOutputPower(outputPower -> {
                byte currentPower = outputPower.getOutputPower()[0];
                if (currentPower > mLowBatterySetPower) {
                    mReader.setOutputPowerUniformly(mLowBatterySetPower, success -> mLowBatterySetPower = 0, null);
                }
            }, null);
            return false;
        }
        return true;
    }

    private void checkTemp() {
        if (mTempThreshold <= 0 || mSaveId < 10) return;

        long now = System.currentTimeMillis();
        if (now - mLastCheckTempMs < 60_000) return;
        mLastCheckTempMs = now;

        mReader.getReaderTemperature(readerTemp -> {
            int temperature = readerTemp.getTemperature();
            if (temperature > mTempThreshold) {
                mReader.getOutputPower(output -> {
                    byte p = output.getOutputPower()[0];
                    p = (byte) (p - mReducePower);
                    if (p < 20) return;
                    mReader.setOutputPowerUniformly(p, null, null);
                    mDelayMs += mAddDelayMs;
                    mInventoryParam.setDelayMs(mDelayMs);
                    if (XLog.sShowLog) {
                        XLog.i("Temperature threshold reached (" + mTempThreshold + "), power reduced to " + p + ", delay increased to " + mDelayMs);
                    }
                }, null);
            }
        }, null);
    }

    // ------------------
    // Utility Methods
    // ------------------

    private void toBeep(int soundFileType) {
        if (mBeepInterval > 0) {
            long now = System.currentTimeMillis();
            if (now - mLastBeepTime > mBeepInterval) {
                mLastBeepTime = now;
                BeeperHelper.beep(soundFileType);
            }
        } else {
            BeeperHelper.beep(soundFileType);
        }
    }

    // ------------------
    // Inner Classes
    // ------------------

    /**
     * Raw reader data consumer (unknown commands handler)
     */
    private static class InnerConsumer<T> implements Consumer<byte[]> {
        int count;

        @Override
        public void accept(byte[] bytes) {
            // Could add processing here if needed
        }

        @Override
        public void onUnknownArr(byte[] bytes) {
            String hex = ArrayUtils.bytesToHexString(bytes, 0, bytes.length);
            XLog.w((++count) + ".UnknownArr: " + hex);
            if (XLog.sShowLog && INSTANCE != null) {
                INSTANCE.mHandler.sendEmptyMessage(INSTANCE.mHandler.MSG_ON_DATA_ERR);
            }
        }
    }

    /**
     * Handler for inventory messages safely dispatching on main thread
     */
    private static class InnerHandler extends Handler {
        static final int MSG_RESUME = 1;
        static final int MSG_ON_RECV = 3;
        static final int MSG_ON_END = 4;
        static final int MSG_ON_DATA_ERR = -1;

        private final WeakReference<InventoryTagPlugin> ref;

        public InnerHandler(InventoryTagPlugin fragment) {
            super(Looper.getMainLooper());
            this.ref = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            InventoryTagPlugin fragment = ref.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case MSG_RESUME:
                    fragment.doNext(true);
                    break;
                case MSG_ON_RECV:
                    fragment.onRecv((InventoryTag) msg.obj);
                    break;
                case MSG_ON_END:
                    fragment.onEnd((InventoryTagEnd) msg.obj);
                    break;
                case MSG_ON_DATA_ERR:
                    if (XLog.sShowLog) {
                        // ToastUtils.makeText(null, "Received unknown/unexpected data!", ToastUtils.LENGTH_LONG);
                    }
                    break;
            }
        }
    }

    // ------------------
    // Cleanup
    // ------------------

    @Override
    protected void handleOnDestroy() {
        INSTANCE = null;
        if (mReader != null && mConsumer != null) {
            mReader.removeOriginalDataReceivedCallback(mConsumer);
        }
        if (mSaveInventoryLog) {
            // Notify system of new log file
            MediaScannerConnection.scanFile(XLog.sContext,
                    new String[]{mLogSavePath}, null, null);
        }
    }
}
