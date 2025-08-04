package com.ultimafurniture.lynx.util;

import com.ultimafurniture.lynx.bean.t30.BarcodeData;
import com.ultimafurniture.lynx.bean.t30.BleAddress;
import com.ultimafurniture.lynx.bean.t30.DevicePower;
import com.ultimafurniture.lynx.bean.t30.ModuleStatus;
import com.ultimafurniture.lynx.bean.t30.RechargeBean;
import com.ultimafurniture.lynx.bean.t30.SnNumber;
import com.ultimafurniture.lynx.bean.t30.TriggerKey;
import com.payne.reader.Reader;
import com.payne.reader.base.Consumer;
import com.payne.reader.bean.config.AntennaCount;
import com.payne.reader.bean.config.ResultCode;
import com.payne.reader.bean.receive.Failure;
import com.payne.reader.bean.receive.ReceiveData;
import com.payne.reader.bean.receive.Success;
import com.payne.reader.bean.receive.Version;
import com.payne.reader.communication.DataPacket;
import com.payne.reader.process.ReaderImpl;
import com.payne.reader.util.ArrayUtils;
import com.payne.reader.util.CheckUtils;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author naz
 *         Date 2020/8/3
 */
public class ReaderHelper {
    private static ReaderHelper mReadHelper = new ReaderHelper();

    private final Reader mReader;
    private final Map<Byte, Consumer> mSuccessMap;
    private final Map<Byte, Consumer> mFailureMap;
    private Consumer<RechargeBean> mRechargeConsumer;
    private Consumer<Boolean> mSleepConsumer;
    /**
     * Save barcode data
     */
    private byte[] mBarcodeData;
    protected volatile ScheduledExecutorService mScheduler;
    private ScheduledFuture<?> mScheduledTask;
    private final Runnable mTask = new Runnable() {
        @Override
        public void run() {
            if (mBarcodeData != null && mBarcodeData.length > 0) {
                if (mBarcodeData.length != 1 || mBarcodeData[0] != 0x0D) {
                    byte cmd = BARCODE_RECEIVE;
                    BarcodeData barcodeData = new BarcodeData();
                    barcodeData.setCmd(cmd);
                    barcodeData.setBarcodeData(new String(mBarcodeData));
                    resultSuccess(cmd, barcodeData);
                }
            }
            mBarcodeData = null;
        }
    };
    /**
     * Whether the interface board is sleeping
     */
    private volatile boolean mIsInterfaceBoardSleep = false;
    private volatile boolean mIsFilterTrigger = true;
    /**
     * Get subversion
     */
    public final static byte GET_FIRMWARE_PATCH_VERSION = (byte) 0xAA;
    /**
     * Get Bluetooth version number
     */
    public final static byte GET_BLUETOOTH_VERSION = (byte) 0xF0;
    /**
     * Get Bluetooth MAC address
     */
    public final static byte GET_BLUETOOTH_MAC_ADDRESS = (byte) 0xF1;
    /**
     * Set Bluetooth broadcast address
     */
    public final static byte SET_BLUETOOTH_BROADCAST_ADDRESS = (byte) 0xF2;
    /**
     * Get interface board SN number
     */
    public final static byte GET_INTERFACE_BOARD_SN_NUMBER = (byte) 0xF3;
    /**
     * Set interface board SN number
     */
    public final static byte SET_INTERFACE_BOARD_SN_NUMBER = (byte) 0xF4;
    /**
     * Shutdown equipment
     */
    public final static byte SHUTDOWN = (byte) 0xF5;
    /**
     * Get interface board version number
     */
    public final static byte GET_INTERFACE_BOARD_VERSION_NUMBER = (byte) 0xF6;
    /**
     * Get battery voltage
     */
    public final static byte GET_BATTERY_VOLTAGE = (byte) 0xF7;
    /**
     * Setting the buzzer
     */
    public final static byte SETTING_BUZZER = (byte) 0xF8;
    /**
     * Enable or Disable rfid / barcode module
     */
    public final static byte OPEN_CLOSE_MODULE = (byte) 0xF9;
    /**
     * Barcode return data
     */
    public final static byte BARCODE_RECEIVE = (byte) 0xFA;
    /**
     * Interface board sleep
     */
    public final static byte INTERFACE_BOARD_SLEEP = (byte) 0xFB;
    /**
     * Trigger key
     */
    public final static byte TRIGGER_KEY = (byte) 0xFC;
    /**
     * Check charge
     */
    public final static byte CHECK_CHARGE = (byte) 0xFF;

    private ReaderHelper() {
        this.mReader = ReaderImpl.create(AntennaCount.SINGLE_CHANNEL);
        this.mSuccessMap = new ConcurrentHashMap<>();
        this.mFailureMap = new ConcurrentHashMap<>();
        this.mReader.setUndefinedResultCallback(this::processUnknownReceive);
    }

    public static ReaderHelper getDefaultHelper() {
        return mReadHelper;
    }

    /**
     * Set result callback
     *
     * @param cmd       Command code，see{@link com.payne.reader.bean.config.Cmd}
     * @param onSuccess Successful result callback
     * @param onError   Failed result callback
     */
    final void setResultCallback(byte cmd, Consumer<? extends Success> onSuccess, Consumer<? extends Failure> onError) {
        if (onSuccess != null) {
            mSuccessMap.put(cmd, onSuccess);
        } else {
            mSuccessMap.remove(cmd);
        }
        if (onError != null) {
            mFailureMap.put(cmd, onError);
        } else {
            mFailureMap.remove(cmd);
        }
    }

    /**
     * Return the reader object in the global reader helper class instance
     *
     * @return Reader
     */
    public static Reader getReader() {
        return getDefaultHelper().mReader;
    }

    /**
     * Get Bluetooth version number
     *
     * @param onSuccess Return success command callback
     * @param onError   Return error command callback
     */
    public void getPatchVersion(Consumer<Version> onSuccess, Consumer<Failure> onError) {
        byte cmd = GET_FIRMWARE_PATCH_VERSION;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, null);
    }

    /**
     * Get Bluetooth version number
     *
     * @param onSuccess Return success command callback
     * @param onError   Return error command callback
     */
    public void getBluetoothVersion(Consumer<Version> onSuccess, Consumer<Failure> onError) {
        byte cmd = GET_BLUETOOTH_VERSION;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, null);
    }

    /**
     * Get Bluetooth MAC address
     *
     * @param onSuccess Return success command callback
     * @param onError   Return error command callback
     */
    public void getBluetoothMacAddress(Consumer<BleAddress> onSuccess, Consumer<Failure> onError) {
        byte cmd = GET_BLUETOOTH_MAC_ADDRESS;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, null);
    }

    /**
     * Set Bluetooth broadcast address
     *
     * @param bleAddress Bluetooth broadcast address, Length limit range 1 ~ 16
     */
    public void setBluetoothBroadcastAddress(String bleAddress) {
        byte[] address = bleAddress.getBytes();
        if (address.length > 16) {
            throw new InvalidParameterException("Incorrect address length!");
        }
        mReader.sendCustomRequest(SET_BLUETOOTH_BROADCAST_ADDRESS, address);
    }

    /**
     * Get interface board SN number
     *
     * @param onSuccess Return success command callback
     * @param onError   Return error command callback
     */
    public void getInterfaceBoardSnNumber(Consumer<SnNumber> onSuccess, Consumer<Failure> onError) {
        byte cmd = GET_INTERFACE_BOARD_SN_NUMBER;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, null);
    }

    /**
     * Set interface board SN number
     *
     * @param hexPasswords Password
     * @param snNumber     SN number
     * @param onSuccess    Return success command callback
     * @param onError      Return error command callback
     */
    public void setInterfaceBoardSnNumber(String hexPasswords, String snNumber, Consumer<Success> onSuccess,
            Consumer<Failure> onError) {
        if (CheckUtils.isNotHexString(hexPasswords)) {
            throw new InvalidParameterException("hexPasswords must be a hexadecimal string!");
        }
        byte[] passwords = ArrayUtils.hexStringToBytes(hexPasswords);
        if (passwords.length > 8) {
            throw new InvalidParameterException("Incorrect password length!");
        }
        if (snNumber == null || snNumber.isEmpty()) {
            throw new InvalidParameterException("snNumber cannot be empty!");
        }
        byte[] btAryData = snNumber.getBytes();
        byte[] btArray = new byte[btAryData.length + 8];
        System.arraycopy(passwords, 0, btArray, 0, passwords.length);
        System.arraycopy(btAryData, 0, btArray, 8, btAryData.length);

        byte cmd = SET_INTERFACE_BOARD_SN_NUMBER;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, btArray);
    }

    /**
     * Turn off the device
     */
    public void shutdown() {
        mReader.sendCustomRequest(SHUTDOWN, null);
    }

    /**
     * Get interface board version number
     *
     * @param onSuccess Return success command callback
     * @param onError   Return error command callback
     */
    public void getInterfaceBoardVersionNumber(Consumer<Version> onSuccess, Consumer<Failure> onError) {
        byte cmd = GET_INTERFACE_BOARD_VERSION_NUMBER;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, null);
    }

    /**
     * Get battery voltage
     *
     * @param onSuccess Return success command callback
     * @param onError   Return error command callback
     */
    public void getDevicePower(Consumer<DevicePower> onSuccess, Consumer<Failure> onError) {
        byte cmd = GET_BATTERY_VOLTAGE;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, null);
    }

    /**
     * Setting the buzzer
     *
     * @param cycles      Number of buzzer sound cycles
     * @param ringingTime Single buzzer duration
     * @param quietTime   Single buzzer stop time
     * @param onSuccess   Return success command callback
     * @param onError     Return error command callback
     */
    public void settingBuzzer(byte cycles, byte ringingTime, byte quietTime, Consumer<Success> onSuccess,
            Consumer<Failure> onError) {
        byte[] btArray = new byte[3];
        btArray[0] = cycles;
        btArray[1] = ringingTime;
        btArray[2] = quietTime;
        byte cmd = SETTING_BUZZER;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, btArray);
    }

    /**
     * Open or close the module
     *
     * @param moduleType 01 RFID 02 barcode
     * @param enable     enable
     * @param autoSleep  autoSleep
     * @param onSuccess  Return success command callback
     * @param onError    Return error command callback
     */
    public void openCloseModule(byte moduleType, boolean enable, boolean autoSleep,
            Consumer<Success> onSuccess, Consumer<Failure> onError) {
        byte[] btArray = new byte[3];
        btArray[0] = moduleType;
        btArray[1] = enable ? (byte) 0x01 : (byte) 0x00;
        btArray[2] = autoSleep ? (byte) 0x01 : (byte) 0x00;

        byte cmd = OPEN_CLOSE_MODULE;
        setResultCallback(cmd, onSuccess, onError);
        DataPacket dataPacket = new DataPacket(mReader.getReaderAddress(), cmd, btArray);
        mReader.sendCustomRequest(dataPacket);
    }

    /**
     * Get module status, open or close
     *
     * @param moduleType 01 RFID 02 barcode
     * @param onSuccess  Return success command callback
     * @param onError    Return error command callback
     */
    public void getModuleStatus(byte moduleType, Consumer<ModuleStatus> onSuccess, Consumer<Failure> onError) {
        byte[] btArray = new byte[1];
        btArray[0] = moduleType;

        byte cmd = OPEN_CLOSE_MODULE;
        setResultCallback(cmd, onSuccess, onError);
        mReader.sendCustomRequest(cmd, btArray);
    }

    /**
     * Set callback for barcode scanning result of 2D head
     *
     * @param onResult Return result callback
     */
    public void setBarcodeResultCallback(Consumer<BarcodeData> onResult) {
        setResultCallback(ReaderHelper.BARCODE_RECEIVE, onResult, null);
    }

    /**
     * Set whether to charge callback
     *
     * @param onResult Return result callback
     */
    public void setRechargeCallback(Consumer<RechargeBean> onResult) {
        this.mRechargeConsumer = onResult;
    }

    /**
     * Set trigger key to enable callback
     *
     * @param onResult Return result callback
     */
    public void setTriggerKeyCallback(Consumer<TriggerKey> onResult) {
        setResultCallback(ReaderHelper.TRIGGER_KEY, onResult, null);
    }

    public void setInterfaceBoardSleep(Consumer<Boolean> onResult) {
        this.mSleepConsumer = onResult;
    }

    public boolean isInterfaceBoardSleep() {
        return mIsInterfaceBoardSleep;
    }

    public void wakeupInterfaceBoard() {
        byte cmd = (byte) 0;
        byte[] data = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        DataPacket packet = new DataPacket(mReader.getReaderAddress(), cmd, data);
        mReader.sendCustomRequest(packet, false);
        mIsInterfaceBoardSleep = false;
    }

    private void processUnknownReceive(ReceiveData receive) {
        switch (receive.getCmd()) {
            case ReaderHelper.GET_FIRMWARE_PATCH_VERSION:
                processGetFirmwarePatchVersion(receive);
                break;
            case ReaderHelper.GET_BLUETOOTH_VERSION:
                processGetBluetoothVersion(receive);
                break;
            case ReaderHelper.GET_BLUETOOTH_MAC_ADDRESS:
                processGetBluetoothMacAddress(receive);
                break;
            case ReaderHelper.SET_BLUETOOTH_BROADCAST_ADDRESS:
                processSetBluetoothBroadcastAddress(receive);
                break;
            case ReaderHelper.GET_INTERFACE_BOARD_SN_NUMBER:
                processGetInterfaceBoardSnNumber(receive);
                break;
            case ReaderHelper.SET_INTERFACE_BOARD_SN_NUMBER:
                processSetInterfaceBoardSnNumber(receive);
                break;
            case ReaderHelper.GET_INTERFACE_BOARD_VERSION_NUMBER:
                processGetInterfaceBoardVersionNumber(receive);
                break;
            case ReaderHelper.GET_BATTERY_VOLTAGE:
                processGetBatteryVoltage(receive);
                break;
            case ReaderHelper.SETTING_BUZZER:
                processSettingBuzzer(receive);
                break;
            case ReaderHelper.OPEN_CLOSE_MODULE:
                processOpenCloseModule(receive);
                break;
            case ReaderHelper.BARCODE_RECEIVE:
                processBarcodeReceive(receive);
                break;
            case ReaderHelper.INTERFACE_BOARD_SLEEP:
                processInterfaceBoardSleep(receive);
                break;
            case ReaderHelper.TRIGGER_KEY:
                processTriggerKey(receive);
                break;
            case ReaderHelper.CHECK_CHARGE:
                processCheckCharge(receive);
                break;
            default:
                if (XLog.sShowLog) {
                    byte[] data = receive.getData();
                    XLog.w(ArrayUtils.bytesToHexString(data, 0, data.length));
                }
        }
    }

    /**
     * Set callback processing separately
     *
     * @param receive Source data received
     */
    private void processSet(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte receiveCmd = receive.getCmd();
        if (dataLen == 1) {
            byte resultCode = data[0];
            if (resultCode == ResultCode.SUCCESS) {
                Success success = new Success();
                success.setCmd(receiveCmd);
                resultSuccess(receiveCmd, success);
            } else {
                resultFailure(receiveCmd, resultCode);
            }
        } else {
            resultFailure(receiveCmd, ResultCode.FAIL);
        }
    }

    private void processGetFirmwarePatchVersion(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte receiveCmd = receive.getCmd();
        if (dataLen == 0x01) {
            String patchVersion = String.valueOf((data[0] & 0xFF));
            Version version = new Version();
            version.setCmd(receiveCmd);
            version.setVersion(patchVersion);
            resultSuccess(receiveCmd, version);
        } else {
            resultFailure(receiveCmd, ResultCode.FAIL);
        }
    }

    private void processGetBluetoothVersion(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte receiveCmd = receive.getCmd();
        if (dataLen > 0) {
            String firmwareVersion = new String(data, 0, dataLen);
            Version version = new Version();
            version.setCmd(receiveCmd);
            version.setVersion(firmwareVersion);
            resultSuccess(receiveCmd, version);
        } else {
            resultFailure(receiveCmd, ResultCode.FAIL);
        }
    }

    private void processGetBluetoothMacAddress(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte receiveCmd = receive.getCmd();
        if (dataLen > 0) {
            String macAddress = new String(data, 0, dataLen);
            BleAddress bleAddress = new BleAddress();
            bleAddress.setCmd(receiveCmd);
            bleAddress.setMacAddress(macAddress);
            resultSuccess(receiveCmd, bleAddress);
        } else {
            resultFailure(receiveCmd, ResultCode.FAIL);
        }
    }

    private void processSetBluetoothBroadcastAddress(ReceiveData receive) {
        XLog.i("processSetBluetoothBroadcastAddress: ");
    }

    private void processGetInterfaceBoardSnNumber(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte receiveCmd = receive.getCmd();
        if (dataLen > 0) {
            String strSnNumber = null;
            String strSnNumber1;
            byte[] bytes = "SA00034".getBytes();
            try {
                strSnNumber = new String(data, 0, dataLen, "ascii");
                strSnNumber1 = new String(data, 0, dataLen, "utf-8");
            } catch (Exception e) {
            }
            SnNumber snNumber = new SnNumber();
            snNumber.setCmd(receiveCmd);
            snNumber.setInterfaceBoardSn(strSnNumber);
            resultSuccess(receiveCmd, snNumber);
        } else {
            resultFailure(receiveCmd, ResultCode.FAIL);
        }
    }

    private void processSetInterfaceBoardSnNumber(ReceiveData receive) {
        processSet(receive);
    }

    private void processGetInterfaceBoardVersionNumber(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte receiveCmd = receive.getCmd();
        if (dataLen > 0) {
            String strVersion = new String(data, 0, dataLen);
            Version version = new Version();
            version.setCmd(receiveCmd);
            version.setVersion(strVersion);
            resultSuccess(receiveCmd, version);
        } else {
            resultFailure(receiveCmd, ResultCode.FAIL);
        }
    }

    private void processGetBatteryVoltage(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte receiveCmd = receive.getCmd();
        if (dataLen > 0) {
            int batteryVoltage = ArrayUtils.byteArrayToInt(data, 0, dataLen, false);
            DevicePower devicePower = new DevicePower();
            devicePower.setCmd(receiveCmd);
            devicePower.setDevicePower(batteryVoltage);
            resultSuccess(receiveCmd, devicePower);
        } else {
            resultFailure(receiveCmd, ResultCode.FAIL);
        }
    }

    private void processSettingBuzzer(ReceiveData receive) {
        processSet(receive);
    }

    private void processOpenCloseModule(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte receiveCmd = receive.getCmd();
        if (dataLen == 2) {
            byte status = data[1];
            if (status == ResultCode.SUCCESS) {
                Success success = new Success();
                success.setCmd(receiveCmd);
                resultSuccess(receiveCmd, success);
            } else if (status == (byte) 1 || status == (byte) 0) {
                ModuleStatus moduleStatus = new ModuleStatus();
                moduleStatus.setCmd(receiveCmd);
                moduleStatus.setModuleType(data[0]);
                moduleStatus.setEnable(status == (byte) 0x01);
                resultSuccess(receiveCmd, moduleStatus);
            } else {
                resultFailure(receiveCmd, status);
            }
        } else {
            resultFailure(receiveCmd, ResultCode.UNKNOWN_ERROR);
        }
    }

    private void processBarcodeReceive(ReceiveData receive) {
        byte[] data = receive.getData();
        if (XLog.sShowLog) {
            XLog.i("processBarcodeReceive: " + ArrayUtils.bytesToHexString(data, 0, data.length));
        }
        if (mScheduledTask != null) {
            mScheduledTask.cancel(true);
            mScheduledTask = null;
        }
        if (data.length > 0) {
            if (mBarcodeData == null) {
                mBarcodeData = data;
            } else {
                byte[] oldData = mBarcodeData;
                mBarcodeData = new byte[oldData.length + data.length];
                System.arraycopy(oldData, 0, mBarcodeData, 0, oldData.length);
                System.arraycopy(data, 0, mBarcodeData, oldData.length, data.length);
            }
            byte endByte = mBarcodeData[mBarcodeData.length - 1];
            if (endByte == (byte) 0x03) {
                int startIndex = 0;
                for (int i = 0; i < mBarcodeData.length; i++) {
                    if (mBarcodeData[i] == (byte) 0x02) {
                        startIndex = i + 1;
                        break;
                    }
                }
                // New data length = Source data length - Data header - Data tail
                int newDataLen = mBarcodeData.length - startIndex - 1;
                if (newDataLen > 0) {
                    byte[] oldData = mBarcodeData;
                    mBarcodeData = new byte[newDataLen];
                    System.arraycopy(oldData, startIndex, mBarcodeData, 0, newDataLen);
                    mTask.run();
                }
            } else if (endByte == (byte) 0x0D) {
                if (mScheduler == null) {
                    mScheduler = Executors.newScheduledThreadPool(1);
                }
                mScheduledTask = mScheduler.schedule(mTask, 50, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void processInterfaceBoardSleep(ReceiveData receive) {
        this.mIsInterfaceBoardSleep = true;
        this.mIsFilterTrigger = true;
        XLog.i("休眠ing...");
        if (mSleepConsumer != null) {
            try {
                mSleepConsumer.accept(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processTriggerKey(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        byte cmd = receive.getCmd();
        if (dataLen == 1) {
            boolean enable = (data[0] & 0xFF) == 0x00;
            if (mIsFilterTrigger) {
                if (!enable) {
                    this.mIsFilterTrigger = false;
                }
                return;
            }
            TriggerKey triggerKey = new TriggerKey();
            triggerKey.setCmd(cmd);
            triggerKey.setEnable(enable);
            resultSuccess(cmd, triggerKey);
        }
    }

    private void processCheckCharge(ReceiveData receive) {
        byte[] data = receive.getData();
        int dataLen = data.length;
        if (dataLen == 1) {
            int switchRecharge = data[0] & 0xFF;
            boolean isRecharge = switchRecharge != 0;
            RechargeBean rechargeBean = new RechargeBean();
            rechargeBean.setRecharge(isRecharge);
            try {
                if (mRechargeConsumer != null) {
                    mRechargeConsumer.accept(rechargeBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Successful execution callback
     *
     * @param cmd     The command code of the data packet returned by the device
     * @param success {@link Success}
     */
    private void resultSuccess(byte cmd, Success success) {
        Consumer<Success> onSuccess = mSuccessMap.get(cmd);
        if (onSuccess != null) {
            try {
                onSuccess.accept(success);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handling return failure generic type callback
     *
     * @param cmd       The command code of the data packet returned by the device
     * @param errorCode Error code, reference{@link ResultCode}
     */
    private void resultFailure(byte cmd, byte errorCode) {
        Failure failure = new Failure();
        failure.setCmd(cmd);
        failure.setErrorCode(errorCode);
        Consumer<Failure> onError = mFailureMap.get(cmd);
        if (onError != null) {
            try {
                onError.accept(failure);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        if (mScheduler != null) {
            mScheduler.shutdown();
            mScheduler = null;
        }
    }
}
