package com.ultimafurniture.lynx.util;

import android.content.Context;

import com.ultimafurniture.lynx.R;
import com.payne.reader.util.ArrayUtils;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * @author naz
 *         Date 2020/1/15
 */
public class RxBleHelper {
    private final static String SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    private final static String NOTIFY_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
    private final static String WRITE_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";

    private static volatile RxBleHelper instance;
    /**
     * 蓝牙{@link RxBleClient}
     */
    private RxBleClient rxBleClient;
    /**
     * 扫描蓝牙{@link Disposable}
     */
    private Disposable scanDisposable;
    /**
     * 蓝牙连接{@link Disposable}
     */
    private Disposable connectDisposable;
    /**
     * 蓝牙连接状态变化{@link Disposable}
     */
    private Disposable connectStateDisposable;
    /**
     * 通知{@link Disposable}
     */
    private Disposable notifyDisposable;
    /**
     * 写数据{@link Disposable}
     */
    private Disposable writeDisposable;

    private RxBleHelper() {
        rxBleClient = RxBleClient.create(XLog.sContext);
    }

    private Consumer<byte[]> mSendConsumer = bytes -> {
        if (XLog.sShowLog) {
            XLog.i("Ble--Send: " + ArrayUtils.bytesToHexString(bytes, 0, bytes.length));
        }
    };
    private Consumer<Throwable> mThrowableConsumer = throwable -> {
        if (XLog.sShowLog) {
            XLog.i("Ble--Send Err: " + throwable.getMessage());
        }
    };

    public static RxBleHelper getInstance() {
        if (instance == null) {
            synchronized (RxBleHelper.class) {
                if (instance == null) {
                    instance = new RxBleHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 扫描蓝牙设备
     *
     * @param scanResultConsumer 订阅事件源，处理蓝牙扫描成功的回调
     * @param throwableConsumer  订阅事件源，处理蓝牙扫描失败的回调
     * @param onFinally          不管成功失败都会执行
     */
    public void startScan(Consumer<ScanResult> scanResultConsumer, Consumer<Throwable> throwableConsumer,
            Action onFinally) {
        stopScan();
        ScanSettings settings = new ScanSettings.Builder()
                // Set scan mode for Bluetooth LE scan.
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
        ScanFilter filter = new ScanFilter.Builder()
                // Set filter on service uuid.
                // .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
                .build();
        scanDisposable = rxBleClient.scanBleDevices(settings, filter)
                .timeout(10000, TimeUnit.MILLISECONDS)
                .filter(scanResult -> {
                    int rssi = scanResult.getRssi();
                    RxBleDevice device = scanResult.getBleDevice();
                    // 搜到的设备,过滤掉信号强度小于-90的设备
                    if (Math.abs(rssi) > 90 || device.getName() == null) {
                        return false;
                    }
                    // 过滤掉不符合的蓝牙设备
                    // String str = "T30";
                    // return device.getName().toUpperCase().startsWith(str);
                    return true;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(onFinally)
                .subscribe(scanResultConsumer, throwableConsumer);
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        if (isScanning()) {
            scanDisposable.dispose();
            scanDisposable = null;
        }
    }

    /**
     * @return 是否正在扫描中
     */
    public boolean isScanning() {
        return scanDisposable != null && !scanDisposable.isDisposed();
    }

    public boolean isConnected(String macAddress) {
        RxBleDevice bleDevice = rxBleClient.getBleDevice(macAddress);
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    /**
     * 蓝牙连接操作并监听连接状态
     *
     * @param macAddress              蓝牙连接地址
     * @param connectionStateConsumer 连接状态
     * @param connectionConsumer      连接成功回调
     * @param throwableConsumer       连接失败回调
     */
    public void connect(String macAddress, Consumer<RxBleConnection.RxBleConnectionState> connectionStateConsumer,
            Consumer<RxBleConnection> connectionConsumer, Consumer<Throwable> throwableConsumer) {
        stopScan();
        if (connectDisposable != null) {
            connectDisposable.dispose();
        }
        RxBleDevice bleDevice = rxBleClient.getBleDevice(macAddress);
        if (bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
            // 蓝牙已连接
            XLog.i("connect: 蓝牙已连接了");
        }
        connectDisposable = bleDevice
                .establishConnection(false)
                .doOnSubscribe(disposable -> connectStateChanges(macAddress, connectionStateConsumer))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectionConsumer, throwableConsumer);
    }

    /**
     * 监听蓝牙连接状态
     *
     * @param macAddress              蓝牙地址
     * @param connectionStateConsumer 蓝牙状态回调
     */
    private void connectStateChanges(String macAddress,
            Consumer<RxBleConnection.RxBleConnectionState> connectionStateConsumer) {
        if (connectStateDisposable != null) {
            connectStateDisposable.dispose();
        }
        connectStateDisposable = rxBleClient.getBleDevice(macAddress)
                .observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectionStateConsumer, mThrowableConsumer);
    }

    /**
     * 通知
     *
     * @param rxBleConnection {@link RxBleConnection}
     * @param dataConsumer    Consumer<byte[]>
     * @param throwable
     */
    public void setupNotification(RxBleConnection rxBleConnection, Consumer<byte[]> dataConsumer,
            Consumer<? super Throwable> throwable) {
        if (notifyDisposable != null) {
            notifyDisposable.dispose();
        }
        notifyDisposable = rxBleConnection.setupNotification(UUID.fromString(NOTIFY_UUID))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataConsumer, throwable);
    }

    /**
     * 写
     *
     * @param rxBleConnection 蓝牙连接处理{@link RxBleConnection}
     * @param btArray         写入字节
     */
    public void write(RxBleConnection rxBleConnection, byte[] btArray) {
        writeDisposable = rxBleConnection.writeCharacteristic(UUID.fromString(WRITE_UUID), btArray)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSendConsumer, mThrowableConsumer);
    }

    /**
     * dispose all
     */
    public static void release() {
        if (instance == null) {
            return;
        }
        instance.realRelease();
    }

    private void realRelease() {
        stopScan();

        if (scanDisposable != null) {
            if (!scanDisposable.isDisposed()) {
                scanDisposable.dispose();
            }
            scanDisposable = null;
        }
        if (connectDisposable != null) {
            if (!connectDisposable.isDisposed()) {
                connectDisposable.dispose();
            }
            connectDisposable = null;
        }
        if (connectStateDisposable != null) {
            if (!connectStateDisposable.isDisposed()) {
                connectStateDisposable.dispose();
            }
            connectStateDisposable = null;
        }
        if (notifyDisposable != null) {
            if (!notifyDisposable.isDisposed()) {
                notifyDisposable.dispose();
            }
            notifyDisposable = null;
        }
        if (writeDisposable != null) {
            if (!writeDisposable.isDisposed()) {
                writeDisposable.dispose();
            }
            writeDisposable = null;
        }
    }

    /**
     * 转换错误信息
     *
     * @param bleScanException 错误信息
     * @return 错误信息
     */
    public String handleBleScanException(BleScanException bleScanException) {
        Context context = XLog.sContext;
        final String text;
        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                text = context.getString(R.string.ble_error_msg_01);
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                text = context.getString(R.string.ble_error_msg_02);
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                text = context.getString(R.string.ble_error_msg_03);
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                text = context.getString(R.string.ble_error_msg_04) + context.getString(R.string.str_location);
                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                text = context.getString(R.string.ble_error_msg_05);
                break;
            case BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                text = context.getString(R.string.ble_error_msg_06);
                break;
            case BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED:
                text = context.getString(R.string.ble_error_msg_07);
                break;
            case BleScanException.SCAN_FAILED_INTERNAL_ERROR:
                text = context.getString(R.string.ble_error_msg_08);
                break;
            case BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                text = context.getString(R.string.ble_error_msg_09);
                break;
            case BleScanException.UNDOCUMENTED_SCAN_THROTTLE:
                text = String.format(
                        Locale.getDefault(),
                        context.getString(R.string.ble_error_msg_10),
                        secondsTill(bleScanException.getRetryDateSuggestion()));
                break;
            case BleScanException.UNKNOWN_ERROR_CODE:
            case BleScanException.BLUETOOTH_CANNOT_START:
                text = context.getString(R.string.no_start_ble);
                break;
            default:
                text = context.getString(R.string.operation_failed) + bleScanException.getMessage();
                break;
        }
        return text;
    }

    private long secondsTill(Date retryDateSuggestion) {
        return TimeUnit.MILLISECONDS.toSeconds(retryDateSuggestion.getTime() - System.currentTimeMillis());
    }
}
