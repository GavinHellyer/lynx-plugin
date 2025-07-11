package com.ultimafurniture.lynx;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginMethod;

import java.io.File;
import java.util.List;

import com.ultimafurniture.lynx.models.OperationTag;
import com.ultimafurniture.lynx.barcode.OrcaAirBarcodeReader;
import com.ultimafurniture.lynx.barcode.helper.ReaderHelper;
import com.ultimafurniture.lynx.barcode.helper.TDCodeTagBuffer;
import com.ultimafurniture.lynx.utils.Beeper;
import com.ultimafurniture.lynx.models.Barcode;
import com.ultimafurniture.lynx.barcode.tools.CalculateSpeed;
import com.ultimafurniture.lynx.utils.AppUtils;
import com.ultimafurniture.lynx.utils.AppLogger;
import com.ultimafurniture.lynx.utils.FileUtils;
import com.ultimafurniture.lynx.models.Inventory;
import com.ultimafurniture.lynx.utils.Constraints;
import com.ultimafurniture.lynx.utils.SessionManager;

@CapacitorPlugin(name = "LynxPlugin")
public class LynxPlugin extends Plugin implements RFIDReaderListener {

    private RFIDReader rfidReader;
    private SessionManager session;
    private LocalBroadcastManager lbm;

    @Override
    public void load() {
        Context context = getContext();
        session = SessionManager.getInstance(context);
        initRfidConnection();
    }

    @PluginMethod
    public void setRfidMode(PluginCall call) {
        initRfidConnection();
        call.resolve();
    }

    @PluginMethod
    public void startRfidScan(PluginCall call) {
        if (rfidReader != null)
            rfidReader.startScan();
        call.resolve();
    }

    @PluginMethod
    public void getRFOutputPower(PluginCall call) {
        int power = session.getIntValue("KEY_RF_OUTPUT_POWER_NEW");
        call.resolve(new JSObject().put("power", power));
    }

    @PluginMethod
    public void setRFOutputPower(PluginCall call) {
        int power = call.getInt("power");
        int status = rfidReader.setRFOutputPower(power);
        call.resolve(new JSObject().put("status", status));
    }

    private void initRfidConnection() {
        rfidReader = new RFIDReader(getContext());
        rfidReader.connect(Reader.ReaderType.RFID);
        rfidReader.setOnRFIDReaderListener(this);
    }

    private void releaseRfidConnection() {
        if (rfidReader != null) {
            rfidReader.releaseResources();
            rfidReader = null;
        }
    }

    private void initializeReceiver() {
        // lbm = LocalBroadcastManager.getInstance(getContext());
        // IntentFilter intentFilter = new IntentFilter();
        // intentFilter.addAction(ReaderHelper.BROADCAST_REFRESH_BAR_CODE);
        // lbm.registerReceiver(barcodeReceiver, intentFilter);
    }

    // Callbacks from RFIDReaderListener
    @Override
    public void onInventoryTag(Inventory inventory) {
        JSObject data = new JSObject();
        data.put("epc", inventory.getFormattedEPC());
        notifyListeners("onInventoryTag", data);
    }

    @Override
    public void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd) {
        JSObject data = new JSObject().put("count", tagEnd.mTagCount);
        notifyListeners("onInventoryTagEnd", data);
    }

    @Override
    public void onOperationTag(OperationTag tag) {
        notifyListeners("onOperationTag", new JSObject().put("epc", tag.strEPC));
    }

    @Override
    public void onConnection(boolean status) {
        notifyListeners("onConnection", new JSObject().put("status", status));
    }

    @Override
    public void onScanningStatus(boolean status) {
        notifyListeners("onScanningStatus", new JSObject().put("status", status));
    }
}