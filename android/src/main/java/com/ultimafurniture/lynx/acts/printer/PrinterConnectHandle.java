package com.ultimafurniture.lynx.acts.printer;

import com.ultimafurniture.lynx.util.XLog;
import com.payne.reader.base.Consumer;
import com.payne.reader.communication.ConnectHandle;
import com.payne.reader.util.ThreadPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class PrinterConnectHandle implements ConnectHandle, Runnable {
    private PrinterInputStream pis;
    private FileOutputStream fos;
    private Consumer<byte[]> mConsumer;

    private AtomicBoolean mIsConnected = new AtomicBoolean();

    public PrinterConnectHandle() {
    }

    @Override
    public boolean onConnect() {
        try {
            fos = new FileOutputStream("/dev/p_app_w_acm");
            pis = new PrinterInputStream(new File("/dev/p_app_r_acm"));

            mIsConnected.set(true);
            ThreadPool.get().execute(this);
        } catch (Exception e) {
            e.printStackTrace();
            mIsConnected.set(false);
        }
        return mIsConnected.get();
    }

    @Override
    public boolean isConnected() {
        return mIsConnected.get();
    }

    @Override
    public boolean onSend(byte[] bytes) {
        try {
            synchronized (this) {
                fos.write(bytes);
                fos.flush();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer) {
        mConsumer = consumer;
    }

    @Override
    public void onDisconnect() {
        try {
            fos.close();
        } catch (Throwable ignored) {
        }
        try {
            pis.close();
        } catch (Throwable ignored) {
        }
        mIsConnected.set(false);
    }

    // byte[] buffer = new byte[1024];
    @Override
    public void run() {
        System.err.println("PCLog_ReceiveThread is run");
        while (mIsConnected.get()) {
            try {
                int available = pis.available();
                XLog.i("available = " + available);
                if (available <= 0) {
                    try {
                        Thread.sleep(100L);
                    } catch (Throwable ignored) {
                    }
                    continue;
                }

                byte[] read = pis.read();

                if (mConsumer != null) {
                    mConsumer.accept(read);
                }
            } catch (Exception e) {
                System.err.println("PCLog_ReceiveThread is error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.err.println("PCLog_ReceiveThread is finished");
    }
}
