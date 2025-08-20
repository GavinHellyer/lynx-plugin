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
    InventoryTagPlugin inventoryTagPlugin;

    @Override
    public void load() {
        Context context = getContext();
        inventoryTagPlugin = InventoryTagPlugin.getInstance();
        initRfidConnection();
    }

    private void initRfidConnection() {
        try {
            XLog.i("initRfidConnection");
            inventoryTagPlugin.setOnRFIDReaderListener(this);
        } catch (Exception e) {
            XLog.i("error while setting listeerns: " + e.getMessage());
            System.out.println(e.getStackTrace());
        }
    }

    @PluginMethod
    public void setRfidMode(PluginCall call) {
        Log.i(TAG, "setRFIDMode Calleddddd");
//        initRfidConnection();

        call.resolve(new JSObject().put("status", true));
    }

    public void startScan(boolean startStop) {
        Log.i(TAG, "StartScan called");
        serialPortActivity.homeActivity.inventoryTagFragment.startStop(startStop);
    }

    @PluginMethod
    public void startRfidScan(PluginCall call) {
        call.resolve();
    }

    @PluginMethod
    public void getRFOutputPower(PluginCall call) {
         call.resolve();
    }

    @PluginMethod
    public void setRFOutputPower(PluginCall call) {
        call.resolve(new JSObject().put("status", true));
    }

    private void releaseRfidConnection() {

    }

    private void initializeReceiver() {

    }

    // Callbacks from RFIDReaderListener
    @Override
    public void onInventoryTag(InventoryTag inventory) {
        XLog.i("onInventoryTag: " + inventory);
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