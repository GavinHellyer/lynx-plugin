package com.ultimafurniture.lynx;

import android.os.SystemClock;
import android.util.Log;

import com.ultimafurniture.lynx.util.FileUtils;
import com.ultimafurniture.lynx.util.XLog;

import java.io.File;
import java.io.PrintWriter;

/**
 * Error Report
 *
 * @author FengLing
 */
public class ErrorReport implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private static class ClassHolder {
        private static final ErrorReport INSTANCE = new ErrorReport();
    }

    private ErrorReport() {
    }

    public static void init() {
        ClassHolder.INSTANCE.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(ClassHolder.INSTANCE);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!handleException(throwable) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, throwable);
        } else {
            if (!"main".equals(thread.getName())) {
                XLog.w("Error Thread = " + thread);
                return;
            }
            SystemClock.sleep(1000);
            ((App) XLog.sContext).onTerminate();
        }
    }

    private boolean handleException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        String msg = Log.getStackTraceString(throwable);
        XLog.e(msg);

        File file = FileUtils.getCrashFile(XLog.sContext);

        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println(msg);
            pw.flush();
        } catch (Throwable ignored) {
        }
        return true;
    }
}