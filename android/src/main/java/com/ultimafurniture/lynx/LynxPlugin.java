package com.ultimafurniture.lynx;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.orhanobut.hawk.Hawk;
import com.payne.reader.bean.receive.InventoryTag;
import com.payne.reader.bean.receive.InventoryTagEnd;
import com.payne.reader.bean.receive.OperationTag;
import com.ultimafurniture.lynx.acts.SerialPortActivity;
import com.ultimafurniture.lynx.util.BeeperHelper;
import com.ultimafurniture.lynx.util.XLog;

@CapacitorPlugin(name = "LynxPlugin")
public class LynxPlugin extends Plugin implements RFIDReaderListener {
    private static final String TAG = "LynxPlugin";

    private LocalBroadcastManager lbm;

    SerialPortActivity serialPortActivity;

    @Override
    public void load() {
        Context context = getContext();

        serialPortActivity = SerialPortActivity.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                initRfidConnection();
            }
        }, 6000);
    }

    private void initRfidConnection() {
        try {
            serialPortActivity.homeActivity.inventoryTagFragment.setOnRFIDReaderListener(this);
        } catch (Exception e) {
            XLog.i("error while setting listeerns: " + e.getMessage());
            System.out.println(e.getStackTrace());
        }
    }

    @PluginMethod
    public void setRfidMode(PluginCall call) {
        Log.i(TAG, "setRFIDMode Calleddddd");
        initRfidConnection();

        call.resolve(new JSObject().put("status", true));
    }

    public void startScan() {
        Log.i(TAG, "StartScan called");
        serialPortActivity.homeActivity.inventoryTagFragment.startStop(true);
        // rfidReader.startScan();
    }

    @PluginMethod
    public void startRfidScan(PluginCall call) {
        startScan();
        call.resolve();
    }

    @PluginMethod
    public void getRFOutputPower(PluginCall call) {
        // rfidReader.getOutputPower();
        // call.resolve();

        // call.resolve(new JSObject().put("power", power));

    }

    @PluginMethod
    public void setRFOutputPower(PluginCall call) {
        // int power = call.getInt("power");
        // int status = rfidReader.setRFOutputPower(power);
        // call.resolve(new JSObject().put("status", status));
        call.resolve(new JSObject().put("status", true));
    }

    private void releaseRfidConnection() {
        // if (rfidReader != null) {
        // // rfidReader.releaseResources();
        // rfidReader = null;
        // }
    }

    private void initializeReceiver() {
        // lbm = LocalBroadcastManager.getInstance(getContext());
        // IntentFilter intentFilter = new IntentFilter();
        // intentFilter.addAction(ReaderHelper.BROADCAST_REFRESH_BAR_CODE);
        // lbm.registerReceiver(barcodeReceiver, intentFilter);
    }

    // Callbacks from RFIDReaderListener
    @Override
    public void onInventoryTag(InventoryTag inventory) {
        JSObject data = new JSObject();
        data.put("epc", inventory.getEpc());
        notifyListeners("onInventoryTag", data);
    }

    @Override
    public void onInventoryTagEnd(InventoryTagEnd tagEnd) {
        JSObject data = new JSObject().put("count", tagEnd.getTotalRead());
        notifyListeners("onInventoryTagEnd", data);
    }

    @Override
    public void onOperationTag(OperationTag tag) {
        notifyListeners("onOperationTag", new JSObject().put("epc", tag));
    }

    @Override
    public void onConnection(boolean status) {
        Log.i(TAG, "onConnection: " + status);
        notifyListeners("onConnection", new JSObject().put("status", status));
    }

    @Override
    public void onOutputPower(int value) {
        Log.i(TAG, "onOutputPower" + value);
        notifyListeners("onOutputPower", new JSObject().put("power", value));
    }

    @Override
    public void onScanningStatus(boolean status) {
        notifyListeners("onScanningStatus", new JSObject().put("status", status));
    }
}