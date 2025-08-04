package com.ultimafurniture.lynx.acts.scanble;

import com.ultimafurniture.lynx.util.RxBleHelper;
import com.payne.reader.base.Consumer;
import com.payne.reader.communication.ConnectHandle;
import com.polidea.rxandroidble2.RxBleConnection;

public class BleConnectHandle implements ConnectHandle {
    private Consumer<byte[]> mConsumer;
    private RxBleConnection mRxBleConnection;

    private io.reactivex.functions.Consumer<byte[]> mDataConsumer = bytes -> {
        if (mConsumer != null) {
            try {
                mConsumer.accept(bytes);
            } catch (Exception ignored) {
            }
        }
    };

    public void setBleConnection(RxBleConnection rxBleConnection,
            io.reactivex.functions.Consumer<? super Throwable> throwable) {
        mRxBleConnection = rxBleConnection;
        RxBleHelper.getInstance().setupNotification(rxBleConnection, mDataConsumer, throwable);
    }

    @Override
    public boolean onConnect() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean onSend(byte[] bytes) {
        RxBleHelper.getInstance().write(mRxBleConnection, bytes);
        return true;
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer) {
        mConsumer = consumer;
    }

    @Override
    public void onDisconnect() {
    }
}