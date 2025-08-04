package com.ultimafurniture.lynx.fragments.home.inventory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.ultimafurniture.lynx.GlobalCfg;
import com.ultimafurniture.lynx.R;
import com.ultimafurniture.lynx.RFIDReaderListener;
import com.ultimafurniture.lynx.bean.InventoryTagBean;
import com.ultimafurniture.lynx.bean.type.Key;
import com.ultimafurniture.lynx.bean.type.LinkType;
import com.ultimafurniture.lynx.util.BeeperHelper;
import com.ultimafurniture.lynx.util.LiveDataBus;
import com.ultimafurniture.lynx.util.ReaderHelper;
import com.ultimafurniture.lynx.util.ShellUtils;
import com.ultimafurniture.lynx.util.ToastUtils;
import com.ultimafurniture.lynx.acts.HomeActivity;
import com.ultimafurniture.lynx.util.ClipboardUtils;
import com.ultimafurniture.lynx.util.InputUtils;
import com.ultimafurniture.lynx.util.XLog;
import com.orhanobut.hawk.Hawk;
import com.payne.reader.Reader;
import com.payne.reader.base.BaseInventory;
import com.payne.reader.base.Consumer;
import com.payne.reader.bean.config.AntennaCount;
import com.payne.reader.bean.config.Beeper;
import com.payne.reader.bean.config.Cmd;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author naz
 *         Date 2020/4/3
 */

import com.getcapacitor.annotation.CapacitorPlugin;

public class InventoryTagFragment extends Plugin {

    private static volatile InventoryTagFragment INSTANCE = null;

    private InventoryTagFragment() {
        // Optional: protect against reflection
        if (INSTANCE != null) {
            throw new RuntimeException("Use getInstance() method");
        }
    }
    public static InventoryTagFragment getInstance() {
        if (INSTANCE == null) {
            synchronized (InventoryTagFragment.class) {
                // Double-check inside synchronized block
                if (INSTANCE == null) {
                    INSTANCE = new InventoryTagFragment();
                }
            }
        }
        return INSTANCE;
    }


    private String TAG = "InventoryTagFragment";
    private static InventoryTagFragment sITF;
    private ConcurrentHashMap<String, InventoryTagBean> mMap = new ConcurrentHashMap<>(999);
    private SimpleDateFormat mSdf = XLog.getSafeDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    private final String PREFIX_EPC_LENGTH_LIMIT = "-1.";

    /**
     * Total tag Count
     */
    private long mTotalCount = 0;

    Context context;

    private int mCountTag;
    private int mEpcLength;
    private volatile boolean mFreezerMode;
    private volatile boolean mOnBaseListLoaded;
    private HashMap<String, Integer> mCurrRecvMap;
    private HashMap<String, Integer> mOverMap;
    private ArrayList<String> mBaseList;

    /* Start RunTime */
    private volatile long mStartRunTime;
    /* Cmd run time */
    private volatile long mCmdStartTime;

    private InventoryParam mInventoryParam;

    private boolean mFastSwitchAntMode;
    private boolean mFastMode;
    private boolean mReadTID;

    private boolean mSaveInventoryLog;
    private long mSaveId;

    private volatile int mBeepInterval;
    private volatile long mLastBeepTime;

    private InnerConsumer<byte[]> mConsumer;
    private LinearLayoutManager mLayoutManager;

    private LinkedList<InventoryTagBean> mData;

    private Reader mReader;
    private String mLogSavePath;
    public String mSelectedEPC;

    private volatile boolean mRunning;
    private volatile byte mLowBatterySetPower;
    private volatile int mDelayMs;

    private String mTempThresholdStr;
    private volatile int mTempThreshold;
    private volatile byte mReducePower;
    private volatile int mAddDelayMs;
    private volatile int mTemperature;
    private volatile long mLastCheckTempMs;

    RFIDReaderListener listener;

    private List<RFIDReaderListener> listeners = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private InnerHandler mHandler = new InnerHandler(this);

    private Comparator<InventoryTagBean> mCpByID = (bean1, bean2) -> bean1.getPosition().compareTo(bean2.getPosition());
    private Comparator<InventoryTagBean> mCpByEpc = (bean1, bean2) -> bean1.getEpc().compareTo(bean2.getEpc());
    private Comparator<InventoryTagBean> mCpByTimes = (bean1, bean2) -> bean2.getTimes().compareTo(bean1.getTimes());

    @Override
    public void load() {

    }

    private Consumer<InventoryTag> mInventoryTagSuccess = inventoryTag -> {
        XLog.i("mInventorySuccess");
        Message msg = Message.obtain();
        msg.what = mHandler.MSG_ON_RECV;
        msg.obj = inventoryTag;
        mHandler.sendMessage(msg);
    };
    private Consumer<InventoryTagEnd> mInventoryTagEndSuccess = inventoryTagEnd -> {
        XLog.i("mInventoryTagEndSuccess");
        Message msg = Message.obtain();
        msg.what = mHandler.MSG_ON_END;
        msg.obj = inventoryTagEnd;
        mHandler.sendMessage(msg);
    };
    private Consumer<OperationTag> mOnReadTagSuccess = new Consumer<OperationTag>() {
        @Override
        public void accept(OperationTag ot) throws Exception {
            XLog.i("mOnReadTagSuccess");
            InventoryTag tag = new InventoryTag();
            tag.setPc(ot.getStrPc());
            tag.setEpc(ot.getStrEpc() + "\n" + ot.getStrData());
            tag.setAntId(ot.getAntId());
            tag.setRssi(ot.getRssi());
            tag.setFreq(ot.getFreq());
            mInventoryTagSuccess.accept(tag);

            if (ot.isEndTag()) {
                if (BeeperHelper.getBeeperType() == Beeper.ONCE_END_BEEP) {
                    toBeep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
                }
                if (mDelayMs == 0) {
                    doNext(true);/* mDelayMs == 0 */
                } else {
                    mHandler.sendEmptyMessageDelayed(mHandler.MSG_RESUME, mDelayMs);
                }
            } else {
                if (BeeperHelper.getBeeperType() == Beeper.PER_TAG_BEEP) {
                    toBeep(BeeperHelper.SOUND_FILE_TYPE_SHORT);
                }
            }
        }
    };

    private Consumer<Failure> failureConsumer = failure -> {
        String resultCodeStr = ResultCode.getNameForResultCode(failure.getErrorCode());
        XLog.w("setInventoryAnt.Failure: " + resultCodeStr);
        doNext(false);/* set ant err */
    };

    private Consumer<Success> successConsumer = success -> {
        if (!mRunning) {
            return;
        }
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

    public static InventoryTagFragment get() {
        return sITF;
    }

    public void init(Context context) {
        this.context = context;

        mReader = ReaderHelper.getReader();
        mConsumer = new InnerConsumer<>();
        mReader.addOriginalDataReceivedCallback(mConsumer);
        mLayoutManager = new LinearLayoutManager(context);

        mInventoryParam = new InventoryParam();


    }

    public void setOnRFIDReaderListener(RFIDReaderListener listener) {
        this.listener = listener;

        listeners.remove(listener);
        listeners.add(listener);
    }

    /**
     * 获取盘存的数据
     *
     * @return List<InventoryTagBean>
     */
    public List<InventoryTagBean> getTags() {
        return mData;
    }

    /**
     * 开始或者停止盘存
     *
     * @param startStop bool
     */
    public boolean startStop(boolean startStop) {
        XLog.i("StartStop calls 264");
        if (!startStop) {
            stopInventory();/* startStop（false） */

            return true;
        }
        if (mRunning) {
            XLog.i("Ignored.running...");
            return true;
        }
        if (mInventoryParam.getInventory() == null) {
            Log.i(TAG, "Inventory parameter configuration error, please check");
            return false;
        }
        if (!mFastSwitchAntMode && !mInventoryParam.hasCheckedAnts()) {
            Log.i(TAG, "Please select at least one antenna");
            return false;
        }

        mRunning = true;

        mLowBatterySetPower = (byte) ((int) Hawk.get(Key.KEY_POWER, 28));
        mBeepInterval = Hawk.get(Key.MINI_NUM_BEEP_TIME, 0);
        mSaveInventoryLog = Hawk.get(Key.SAVE_INVENTORY_LOG, false);
        mTempThresholdStr = Hawk.get(Key.KEY_TEMP_THRESHOLD, "45,3,2000");
        if (!TextUtils.isEmpty(mTempThresholdStr)) {
            try {
                String[] split = mTempThresholdStr.split(",");
                int t = Integer.parseInt(split[0]);
                int p = Integer.parseInt(split[1]);
                int ms = Integer.parseInt(split[2]);
                mTempThreshold = t;
                mReducePower = (byte) p;
                mAddDelayMs = ms;
            } catch (Exception ignored) {
            }
        }
        try {
            mDelayMs = 10;
        } catch (Exception e) {
            mDelayMs = 0;
        }
        mInventoryParam.setDelayMs(mDelayMs);

        if (mSaveInventoryLog) {
            mSaveId = 1;
            // String timeStr = XLog.getSafeDateFormat("yyyy-MM-dd").format(new Date());
            // String path =
            // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            // mLogSavePath = path + File.separator + "InventoryLog" + File.separator +
            // timeStr + ".txt";
            mLogSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                    + "InventoryTesting-log.txt";

            ThreadPool.get().execute(() -> {
                StringBuilder title = new StringBuilder();
                title.append(mFastSwitchAntMode ? "------快速切换多天线盘存" : "------自定义Session盘存");

                if (mFreezerMode) {
                    title.append(".冰柜");
                }
                title.append(".延迟(ms):").append(mDelayMs)
                        .append(".repeat:").append(mInventoryParam.getRepeat())
                        .append(".loop:").append(mInventoryParam.getLoopCount())
                        .append("\n");
                saveInventoryLog(title.toString());
            });
        }

        setInventoryConfig(mFastMode);

        mStartRunTime = System.currentTimeMillis();

        return true;
    }

    private void doNext(boolean nextAntId) {
        if (!mRunning) {
            return;
        }
        int tmpAntennaId = mInventoryParam.getAntennaId(nextAntId);
        mReader.setWorkAntenna(tmpAntennaId, successConsumer, failureConsumer);
    }

    private void setInventoryConfig(boolean isFast) {
        try {
            XLog.i("setInventoryConfig");
            XLog.i(mInventoryParam.getInventory().toString());

            InventoryConfig config = new InventoryConfig.Builder()
                    .setInventoryParam(mInventoryParam)
                    .setInventory(mInventoryParam.getInventory())
                    .setOnInventoryTagSuccess(mInventoryTagSuccess)
                    .setOnInventoryTagEndSuccess(mInventoryTagEndSuccess)
                    .setOnFailure(failure -> {
                        XLog.e("setInventoryError" + failure.getErrorCode());
                        onFailure(failure);
                    })
                    .setFastInventory(isFast)/* 只支持单天线，适用于E710、FDW模块,且需要在setInventory之后调用 */
                    .build();
            mReader.setInventoryConfig(config);
            XLog.i("Inventoryconfig set");

        } catch (Exception e) {
            XLog.e(e.getMessage());
        }
    }

    private void onFailure(InventoryFailure failure) {
        if (XLog.sShowLog) {
            byte errorCode = failure.getErrorCode();

            byte cmd = failure.getCmd();
            int antId = failure.getAntId();

            String cmdStr = Cmd.getNameForCmd(cmd);
            String resultCodeStr = ResultCode.getNameForResultCode(errorCode);
            XLog.w("OnFailure: Ant（" + antId + "）" + cmdStr + "-->" + resultCodeStr);
        }
        if (mFastSwitchAntMode) {
            XLog.w("onFailure but SDK will retry when mFastSwitchAntMode is true, No need to call startInventory again.");
            return;
        }

        doNext(true);/* onFailure */
    }

    private void onRecv(InventoryTag inventoryTag) {
        XLog.w("onRecv: " + inventoryTag);
        if (BeeperHelper.getBeeperType() == Beeper.PER_TAG_BEEP) {
            toBeep(BeeperHelper.SOUND_FILE_TYPE_SHORT);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (RFIDReaderListener l : listeners) {
                    l.onScanningStatus(true);
                    l.onInventoryTag(inventoryTag);
                }
            }
        });

    }

    private void onEnd(InventoryTagEnd inventoryTagEnd) {

        int totalRead = inventoryTagEnd.getTotalRead();
        if (totalRead > 0 && BeeperHelper.getBeeperType() == Beeper.ONCE_END_BEEP) {
            toBeep(BeeperHelper.SOUND_FILE_TYPE_NORMAL);
        }
        boolean isGroupEnd = isGroupEnd(inventoryTagEnd);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                for (RFIDReaderListener l : listeners) {
                    l.onInventoryTagEnd(inventoryTagEnd);
                }
            }
        },0);

        if (XLog.sShowLog || mSaveInventoryLog) {
            endLog(inventoryTagEnd, isGroupEnd);
        }

        if (!mRunning || inventoryTagEnd.isFinished()) {
            stopInventory();
            return;
        }

        if (isGroupEnd) {
            checkBattery();
            checkTemp();
        }
        if (mFastSwitchAntMode) {
            if (mFreezerMode && isGroupEnd) {
            }
            // When mFastSwitchAntMode is true,No need to call startInventory again !!!
            return;
        }
        if (mDelayMs == 0) {
            doNext(true);/* mDelayMs == 0 */
        } else {
            mHandler.sendEmptyMessageDelayed(mHandler.MSG_RESUME, mDelayMs);
        }
    }

    private boolean isGroupEnd(InventoryTagEnd inventoryTagEnd) {
        boolean isGroupEnd;
        if (mFastSwitchAntMode) {
            if (mReader.getAntennaCount() == AntennaCount.SIXTEEN_CHANNELS) {
                int antennaGroupId = inventoryTagEnd.getAntennaGroupId();
                isGroupEnd = antennaGroupId == 1;
            } else {
                isGroupEnd = true;
            }
        } else {
            isGroupEnd = mInventoryParam.isLastAnt();
        }
        return isGroupEnd;
    }

    private void endLog(InventoryTagEnd inventoryTagEnd, boolean isGroupEnd) {
        long ctm = System.currentTimeMillis();
        int size = mData.size();

        int totalRead = inventoryTagEnd.getTotalRead();
        int readRate = inventoryTagEnd.getReadRate();
        int cmdTime = 0;
        if (readRate > 0) {
            cmdTime = (int) (totalRead * 1000.0 / readRate);
        }
        int antennaGroupId = inventoryTagEnd.getAntennaGroupId();
        String suffixStr = "] ";

        StringBuilder msgSb = new StringBuilder();
        msgSb.append("时间:[").append(mSdf.format(new Date())).append(suffixStr)
                .append("ID:[").append(mSaveId++).append(suffixStr)
                .append("工作天线组:[").append(antennaGroupId).append(suffixStr);
        if (!mFastSwitchAntMode) {
            msgSb.append("工作天线:[").append(inventoryTagEnd.getCurrentAnt()).append(suffixStr);
        }
        msgSb.append("本轮识别速率:[").append(inventoryTagEnd.getReadRate()).append(suffixStr)
                .append("本轮指令耗时:[").append(cmdTime).append(suffixStr)
                .append("累计运行时长:[").append(ctm - mStartRunTime).append("ms] ")
                .append("累计接收总数:[").append(mTotalCount).append(suffixStr)
                .append("本轮读取标签:[").append(inventoryTagEnd.getTotalRead()).append(suffixStr)
                .append("去重后实际数:[").append(size).append(suffixStr).append("\n");
        if (isGroupEnd) {
            msgSb.append("\n");
        }
        if (mCountTag > 0) {
            addFreezerLog(size, msgSb);
        }

        String msg = msgSb.toString();
        XLog.i(msg);

        if (mSaveInventoryLog) {
            saveInventoryLog(msg);
        }
    }

    private void addFreezerLog(int size, StringBuilder msgSb) {
        int baseSize = mBaseList.size();
        if (baseSize == 0) {
            if (mCountTag == size) {
                msgSb.append("基数列表：\n");
                for (int i = 0; i < mCountTag; i++) {
                    InventoryTagBean bean = mData.get(i);
                    String epc = bean.getEpc();
                    msgSb.append(i + 1).append("、").append(epc).append("\n");
                    mBaseList.add(epc);
                }
                mOnBaseListLoaded = true;
                startStop(false);
            }
        } else {
            int overSize = mOverMap.size();
            if (overSize > 0) {
                msgSb.append("读到了，但不在基数列表的EPC：\n");
                Set<Map.Entry<String, Integer>> entries = mOverMap.entrySet();
                int i = 1;
                for (Map.Entry<String, Integer> entry : entries) {
                    msgSb.append(i++).append("、").append(entry).append("\n");
                }
                mOverMap.clear();
            }
            int validCount = 0;
            int recvSize = mCurrRecvMap.size();
            if (recvSize > 0) {
                boolean addTitle = true;
                for (String epc : mBaseList) {
                    if (mCurrRecvMap.containsKey(epc)) {
                        validCount++;
                    } else {
                        if (addTitle) {
                            addTitle = false;
                            msgSb.append("未读到的EPC：\n");
                        }
                        msgSb.append(epc).append("\n");
                    }
                }
                mCurrRecvMap.clear();
            }
            if (validCount == 0) {
                msgSb.append("本轮读取，全部丢失!\n");
            }
        }
    }

    // <editor-fold desc="checkBattery">
    private boolean checkBattery() {
        if (mLowBatterySetPower <= 0) {
            return true;
        }
        int batteryLevel = HomeActivity.getBatteryLevel();
        if (batteryLevel > 0 && batteryLevel < 11) {
            Log.i(TAG, "Low Battery");
            mReader.getOutputPower(new Consumer<OutputPower>() {
                @Override
                public void accept(OutputPower outputPower) throws Exception {
                    byte p = outputPower.getOutputPower()[0];
                    if (p > mLowBatterySetPower) {
                        Consumer<Success> consumer = success -> mLowBatterySetPower = 0;
                        mReader.setOutputPowerUniformly(mLowBatterySetPower, consumer, null);
                    } else {
                        XLog.i("Ignored.mCurrPower already less than mSetPower");
                    }
                }
            }, null);
            // if (mAlertDialog == null) {
            // mAlertDialog = new AlertDialog.Builder(getActivity())
            // .setTitle(R.string.alert_dialog_title)
            // .setMessage(R.string.battery_low)
            // .setPositiveButton(R.string.sure, (dialog, which) -> {
            //// mLowBatteryMonitor = 0;
            // })
            // .setNegativeButton(R.string.cancel, (dialog, which) -> {
            // stopInventory();/*Low Battery*/
            // })
            // .show();
            // } else if (!mAlertDialog.isShowing()) {
            // mAlertDialog.show();
            // }
            return false;
            // } else if (mCurrPower == 10) {
            // mSetPower = 30;
            // mReader.setOutputPowerUniformly(mSetPower, true, mOnSetPowerSuccess,
            // mOnSetPowerError);
        }
        return true;
    }
    // </editor-fold>

    // <editor-fold desc="checkTemp">
    private Consumer<ReaderTemperature> mTemperatureConsumer;

    private void checkTemp() {
        if (mTempThreshold <= 0 || mSaveId < 10) {
            return;
        }
        long l = System.currentTimeMillis();
        long v = l - mLastCheckTempMs;
        if (v < 1 * 60 * 1000) {
            return;
        }
        mLastCheckTempMs = l;
        if (mTemperatureConsumer == null) {
            mTemperatureConsumer = new Consumer<ReaderTemperature>() {
                @Override
                public void accept(ReaderTemperature readerTemperature) throws Exception {
                    int temperature = readerTemperature.getTemperature();
                    if (temperature > mTempThreshold) {
                        XLog.w("温度过高,temperature=" + temperature);
                        Consumer<OutputPower> powerConsumer = new Consumer<OutputPower>() {
                            @Override
                            public void accept(OutputPower outputPower) throws Exception {
                                byte p = outputPower.getOutputPower()[0];
                                p = (byte) (p - mReducePower);
                                if (p < 20) {
                                    return;
                                }
                                mReader.setOutputPowerUniformly(p, null, null);
                                mDelayMs = mDelayMs + mAddDelayMs;
                                mInventoryParam.setDelayMs(mDelayMs);
                                if (XLog.sShowLog) {
                                    String msg = "温度达到阈值：" + mTempThreshold + ",功率减至" + p + ",延时增至" + mDelayMs;
                                    XLog.i(msg);
                                }
                            }
                        };
                        mReader.getOutputPower(powerConsumer, null);
                    }
                }
            };
        }
        mReader.getReaderTemperature(mTemperatureConsumer, null);
    }
    // </editor-fold>

    private void toBeep(int soundFileType) {
        if (mBeepInterval > 0) {
            long nowMs = System.currentTimeMillis();
            long ms = nowMs - mLastBeepTime;
            if (ms > mBeepInterval) {
                // XLog.w("It's time to beep------------------");
                mLastBeepTime = nowMs;
                BeeperHelper.beep(soundFileType);
            }
        } else {
            BeeperHelper.beep(soundFileType);
        }
    }

    public String formatSeconds(long totalSeconds) {
        totalSeconds = totalSeconds / 1000;
        if (totalSeconds < 1) {
            return "0s";
        }

        long hours = totalSeconds / 3600;

        long rem = totalSeconds % 3600;
        long minutes = rem / 60;
        long seconds = rem % 60;

        if (minutes == 0 && hours == 0) {
            return String.format("%02ds", seconds);
        }
        if (hours == 0) {
            return String.format("%02dm:%02ds", minutes, seconds);
        }
        return String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
    }

    private void stopInventory() {
        // try {
        // int i = 1 / 0;
        // } catch (Exception e) {
        // XLog.w("stopInventory" + Log.getStackTraceString(e));
        // }
        mConsumer.count = 0;
        mHandler.removeMessages(mHandler.MSG_RESUME);
        mHandler.removeMessages(mHandler.MSG_STOP_ON_TEMP);
        mHandler.removeMessages(mHandler.MSG_START_INVENTORY);

        if (mRunning && mReader != null) {
            mReader.stopInventory();
        }
        mRunning = false;
    }

    public void onDestroy() {
        sITF = null;

        mHandler.removeCallbacksAndMessages(null);
        mReader.removeOriginalDataReceivedCallback(mConsumer);
        if (mSaveInventoryLog) {
            MediaScannerConnection.scanFile(XLog.sContext, new String[] { mLogSavePath }, null, null);
        }
    }

    // <editor-fold desc="saveInventoryLog">
    private void saveInventoryLog(String strLog) {
        XLog.i(strLog);
        OutputStream os = null;
        try {
            File file = new File(mLogSavePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            os = new FileOutputStream(file, true);
            os.write(strLog.getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="onUnknownArr">
    private static class InnerConsumer<T> implements Consumer<byte[]> {
        int count;

        @Override
        public void accept(byte[] bytes) throws Exception {
        }

        @Override
        public void onUnknownArr(byte[] bytes) throws Exception {
            String hexStr = ArrayUtils.bytesToHexString(bytes, 0, bytes.length);
            XLog.w((++count) + ".onUnknownArr: " + hexStr);
            if (XLog.sShowLog) {
                sITF.mHandler.sendEmptyMessage(sITF.mHandler.MSG_ON_DATA_ERR);/* 收到未知指令 */
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="InnerHandler">
    private static class InnerHandler extends Handler {
        private final int MSG_ON_DATA_ERR = -1;
        private final int MSG_REFRESH = 0;
        private final int MSG_RESUME = 1;
        private final int MSG_STOP_ON_TEMP = 2;
        private final int MSG_ON_RECV = 3;
        private final int MSG_ON_END = 4;
        private final int MSG_START_INVENTORY = 5;
        private final int MSG_STOP_INVENTORY = 6;
        private final int MSG_SHOW_TEMPERATURE = 7;

        private WeakReference<InventoryTagFragment> mWr;

        public InnerHandler(InventoryTagFragment fragment) {
            mWr = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            InventoryTagFragment f = mWr.get();
            if (f == null) {
                return;
            }
            switch (msg.what) {
                case MSG_ON_DATA_ERR:
                    if (XLog.sShowLog) {
                        String errStr = "存在解析异常的数据!!!";
                        ToastUtils.makeText(null, "" + errStr, Toast.LENGTH_LONG).show();
                    }
                    break;
                case MSG_REFRESH:
                    break;
                case MSG_RESUME:
                    f.doNext(true);/* mDelayMs > 0 */
                    break;
                case MSG_ON_RECV:
                    f.onRecv((InventoryTag) msg.obj);
                    break;
                case MSG_ON_END:
                    f.onEnd((InventoryTagEnd) msg.obj);
                    break;
                case MSG_START_INVENTORY: {
                    f.startStop(true); /* 收到盘存消息，开始盘存 */
                }
                    break;
                case MSG_STOP_INVENTORY: {
                    f.startStop(false); /* 收到结束消息，结束盘存 */
                }
                    break;
                case MSG_STOP_ON_TEMP: {
                    if (f.mStartRunTime == (Long) msg.obj) {
                        f.startStop(false);/* 定时停止 */
                        if (msg.arg1 == -9) {
                            Consumer<ReaderTemperature> callback = new Consumer<ReaderTemperature>() {
                                @Override
                                public void accept(ReaderTemperature readerTemperature) throws Exception {

                                    int t = readerTemperature.getTemperature();
                                    if (f.mTemperature <= 0) {
                                        f.mTemperature = t;
                                        f.mHandler.sendEmptyMessage(f.mHandler.MSG_START_INVENTORY);
                                    } else {
                                        Message msg = Message.obtain();
                                        msg.what = f.mHandler.MSG_SHOW_TEMPERATURE;
                                        msg.arg1 = t;
                                        f.mHandler.sendMessage(msg);
                                    }
                                }
                            };
                            ReaderHelper.getReader().getReaderTemperature(callback, null);
                        }
                    }
                }
                    break;
                case MSG_SHOW_TEMPERATURE: {
                    int tempOffset = msg.arg1 - f.mTemperature;
                    String text = "开始温度：" + f.mTemperature + "，结束温度：" + msg.arg1 + "\n实际温差：" + tempOffset;
                    Log.i("InventoryTagFragment", text);
                }
                    break;
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="loadBaseList">
    private void loadBaseList(ArrayList<String> list) {
    }
    // </editor-fold>
}