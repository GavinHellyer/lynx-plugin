package com.ultimafurniture.lynx.util;

import android.util.Log;

import java.io.Closeable;

public class CloseUtils {
    private CloseUtils() {
    }

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void close(Closeable... closeables) {
        if (closeables == null)
            return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    XLog.w(Log.getStackTraceString(e));
                }
            }
        }
    }

    /**
     * 安静关闭IO
     *
     * @param closeables closeable
     */
    public static void closeSilent(Closeable... closeables) {
        if (closeables == null)
            return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {//
                }
            }
        }
    }
}
