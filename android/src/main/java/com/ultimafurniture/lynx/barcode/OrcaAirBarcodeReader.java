package com.ultimafurniture.lynx.barcode;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.nativec.tools.ModuleManager;
import com.nativec.tools.SerialPort;
import com.nativec.tools.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

// import com.ultimafurniture.lynx.R;
import com.ultimafurniture.lynx.Reader;
import com.ultimafurniture.lynx.barcode.base.ConstantFlag;
import com.ultimafurniture.lynx.barcode.base.ReaderBase;
import com.ultimafurniture.lynx.barcode.helper.ReaderHelper;
import com.ultimafurniture.lynx.barcode.helper.ScannerSetting;
import com.ultimafurniture.lynx.barcode.helper.TDCodeTagBuffer;
import com.ultimafurniture.lynx.barcode.tools.PreferenceUtil;
import com.ultimafurniture.lynx.utils.Beeper;
import com.ultimafurniture.lynx.utils.AppLogger;

public class OrcaAirBarcodeReader implements Reader {

    private static final String TAG = "OrcaAirBarcodeReader";

    private static final int BAUD_RATE = 9600;
    private static final String PORT = "dev/ttyS1";
    private ReaderHelper mReaderHelper;
    private ReaderBase mReader;
    private final Handler mHandler;

    private static SerialPort mSerialPort = null;
    private List<String> mPortList = new ArrayList<String>();

    private SerialPortFinder mSerialPortFinder;
    private int mPosPort = -1;
    private AppLogger logger;

    String[] entries = null;
    String[] entryValues = null;

    // the buffer of 2D code and bar code data;
    public static TDCodeTagBuffer mTagBuffer;

    private Context context;

    public OrcaAirBarcodeReader(Context context) {

        this.context = context;
        PreferenceUtil.init(context);
        logger = AppLogger.getInstance();

        try {
            ReaderHelper.setContext(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Beeper.init(context);
        mSerialPortFinder = new SerialPortFinder();
        entries = mSerialPortFinder.getAllDevices();
        entryValues = mSerialPortFinder.getAllDevicesPath();
        mHandler = new Handler(context.getMainLooper());
    }

    void initConnection() {

        try {
            if (ModuleManager.newInstance().getUHFStatus()) {
                boolean uhfStatus = ModuleManager.newInstance().setUHFStatus(false);
            }
            ModuleManager.newInstance().setScanAction(true);
        } catch (Exception e) {
            e.printStackTrace();
            logger.i(TAG, "initConnection: Please exit UHFDemo first!");
            return;
        }

        try {

            mSerialPort = new SerialPort(new File(PORT), BAUD_RATE, 0);

            ModuleManager.newInstance().setScanStatus(true);
            ModuleManager.newInstance().setScanAction(true);
            if (!ModuleManager.newInstance().setScanStatus(true)) {
                throw new RuntimeException("Scan power on failure,may you open in other" +
                        "Process and do not exit it");
            }

            try {
                mReaderHelper = ReaderHelper.getDefaultHelper();
                mReaderHelper.setReader(mSerialPort.getInputStream(), mSerialPort.getOutputStream());
                mReader = mReaderHelper.getReader();

                mTagBuffer = mReaderHelper.getCurOperateTagBinDCodeBuffer();
            } catch (Exception e) {
                e.printStackTrace();
                logger.i(TAG, "initConnection: Connection Not Established: " + e.getLocalizedMessage());
                return;
            }

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        if (!PreferenceUtil.getBoolean(ConstantFlag.IS_FIRST_OPEN, false)) {
                            Thread.currentThread().sleep(300);
                            ScannerSetting.newInstance().defaultSettings();
                            PreferenceUtil.commitBoolean(ConstantFlag.IS_FIRST_OPEN, true);
                        }
                        Thread.currentThread().sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    logger.i(TAG, "=== BARCODE === Connection Established Successfully");
                }
            });

            th.start();

            // finish();
        } catch (SecurityException e) {
            // logger.i(TAG, "initConnection: " + R.string.error_security);
        } catch (IOException e) {
            // logger.i(TAG, "initConnection: " + R.string.error_unknown);
        } catch (InvalidParameterException e) {
            // logger.i(TAG, "initConnection: " + R.string.error_configuration);
        } catch (Exception e) {
            logger.i("where the exception!", "is here" + e.getLocalizedMessage());
            /* catch exception test */
        }
    }

    public void releaseResources() {

        // if (mReader != null)
        // mReader.signOut();
        // if (mSerialPort != null)
        // mSerialPort.close();
        // mSerialPort = null;
        ModuleManager.newInstance().setScanStatus(false);
        // ModuleManager.newInstance().release();
        // Beeper.release();
        logger.i(TAG, "releaseResources: released");
    }

    void resumeConnection() {
        if (mReader != null) {
            if (!mReader.IsAlive())
                mReader.StartWait();
        }
    }

    void pauseConnection() {
        ModuleManager.newInstance().setScanStatus(false);
    }

    public void clearBuffer() {
        mTagBuffer.clearBuffer();
    }

    @Override
    public void connect(ReaderType type) {
        initConnection();
    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
