package com.ultimafurniture.lynx.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import com.payne.reader.util.LLLog;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * adb开启日志：adb shell setprop log.tag.PCLog_ DEBUG
 * adb开启日志写入文件：adb shell setprop log.tag.PCLog_Save DEBUG
 */
public class XLog {
    private static final String TAG = "PCLog_";
    private static final String TAG_TO_FILE_ = "PCLog_Save";
    /**
     * 最大保存文件条数
     */
    private static final int FILES_LENGTH = 10;
    /**
     * 是否显示日志
     */
    public static volatile boolean sShowLog;
    private static volatile boolean sShowSDKLog;
    /**
     * 是否将日志写入文件
     */
    private static volatile boolean sSaveLogToFile;
    /**
     * 日志文件路径
     */
    private static File sLocalLogDir;
    private static LinkedBlockingQueue<Runnable> sWorkQueue;
    private volatile static ExecutorService sExecutor;

    public static Context sContext;

    private static final ThreadLocal<Map<String, SimpleDateFormat>> SDF_THREAD_LOCAL = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        @Override
        protected Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<>();
        }
    };

    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat getSafeDateFormat(String pattern) {
        Map<String, SimpleDateFormat> sdfMap = SDF_THREAD_LOCAL.get();
        // No Inspection ConstantConditions
        SimpleDateFormat simpleDateFormat = sdfMap.get(pattern);
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat(pattern);
            sdfMap.put(pattern, simpleDateFormat);
        }
        return simpleDateFormat;
    }

    public static void init(Context context, boolean showLog, boolean saveLog) {
        sContext = context.getApplicationContext();
        sShowLog = showLog;
        sSaveLogToFile = saveLog;

        if (!sShowLog) {
            sShowLog = Log.isLoggable(TAG, Log.DEBUG);
        }
        if (!sSaveLogToFile) {
            sSaveLogToFile = saveLog || Log.isLoggable(TAG_TO_FILE_, Log.DEBUG);
        }

        createEx();
    }

    public static void i(String msg) {
        Log.i("XLog", msg);
        i(msg, 5);
    }

    public static void i(String tag, String msg) {
        i("[" + tag + "]" + msg, 5);
    }

    public static void i(String msg, int index) {
        if (sShowLog || sSaveLogToFile) {
            msg = "[" + getStackTrace(index) + "]\n" + msg;
        }
        if (sShowLog) {
            if (msg.length() > 3000) {
                boolean needPrefix = false;
                while (true) {
                    if (msg.length() > 3000) {
                        String logStr = msg.substring(0, 3000);
                        Log.i(TAG, needPrefix ? "[接上文]" + logStr : logStr);
                        msg = msg.substring(3000);
                    } else {
                        Log.i(TAG, "[接上文]" + msg + "\n---------------------------------------------------------------");
                        break;
                    }
                    needPrefix = true;
                }
            } else {
                Log.i(TAG, msg);
            }
        }
        if (sSaveLogToFile) {
            writeLocalLog(getCurrentFormattedTime(), "_i:", msg);
        }
    }

    public static void d(String msg) {
        Log.d("XLog", msg);
        d(msg, 5);
    }

    public static void d(String tag, String msg) {
        d("[" + tag + "]" + msg, 5);
    }

    public static void d(String msg, int index) {
        if (sShowLog || sSaveLogToFile) {
            msg = "[" + getStackTrace(index) + "]\n" + msg;
        }
        if (sShowLog) {
            if (msg.length() > 3000) {
                boolean needPrefix = false;
                while (true) {
                    if (msg.length() > 3000) {
                        String logStr = msg.substring(0, 3000);
                        Log.d(TAG, needPrefix ? "[接上文]" + logStr : logStr);
                        msg = msg.substring(3000);
                    } else {
                        Log.d(TAG, "[接上文]" + msg + "\n---------------------------------------------------------------");
                        break;
                    }
                    needPrefix = true;
                }
            } else {
                Log.d(TAG, msg);
            }
        }
        if (sSaveLogToFile) {
            writeLocalLog(getCurrentFormattedTime(), "_d:", msg);
        }
    }

    public static void w(String msg) {
        Log.i("XLog",msg);
        w(msg, 5);
    }

    public static void w(String tag, String msg) {
        w("[" + tag + "]" + msg, 5);
    }

    public static void w(String msg, int index) {
        if (sShowLog || sSaveLogToFile) {
            msg = "[" + getStackTrace(index) + "]\n" + msg;
        }
        if (sShowLog) {
            Log.w(TAG, msg);
        }
        if (sSaveLogToFile) {
            writeLocalLog(getCurrentFormattedTime(), "_w:", msg);
        }
    }

    public static void e(String msg) {
        Log.e("XLog", msg);
        e(msg, 5);
    }

    public static void e(String tag, String msg) {
        e("[" + tag + "]" + msg, 5);
    }

    public static void e(String msg, int index) {
        if (sShowLog || sSaveLogToFile) {
            msg = "[" + getStackTrace(index) + "]\n" + msg;
        }
        if (sShowLog) {
            Log.e(TAG, "catchInfo------------->\n" + msg);
        }
        if (sSaveLogToFile) {
            writeLocalLog(getCurrentFormattedTime(), "_e:", msg);
        }
    }

    private static String writeLocalLog(String date, String level, String msg) {
        if (sLocalLogDir == null) {
            Log.e(TAG, "LOCAL_LOG_DIR 路径异常, 无法写日志");
            return null;
        }
        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                String fileName = getCurrentDate() + ".txt";
                File file = new File(sLocalLogDir, fileName);
                writeLocalLog(file, date, level, msg);
                // System.out.printf("-------------------------已完成任务: %s%s%s\n", date, level,
                // msg);
                return date;
            }
        };
        addTask(task);
        return null;
    }

    private static volatile Future<String> lastSubmit = null;

    private static void addTask(Callable<String> task) {
        try {
            lastSubmit = sExecutor.submit(task);
        } catch (RejectedExecutionException ignored) {
            if (sShowLog) {
                int size = sWorkQueue.size();
                Log.w(TAG, "任务添加失败,队列[" + size + "]");
            }

            try {
                lastSubmit.get();
            } catch (Exception e) {
                Log.w(TAG, Log.getStackTraceString(e));
            }

            addTask(task);
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    private static void deleteOldestFile() {
        if (sLocalLogDir == null || !sLocalLogDir.exists() || !sLocalLogDir.isDirectory()) {
            return;
        }
        File[] files = sLocalLogDir.listFiles();
        if (files == null || files.length <= FILES_LENGTH) {
            return;
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });
        // for (File file : files) {
        // Log.i(TAG, "after sort->" + file);
        // }

        for (int i = 0; i < files.length - FILES_LENGTH; i++) {
            if (!files[i].delete()) {
                Log.w(TAG, "delete.err->" + files[i]);
            }
        }
    }

    private static void writeLocalLog(File file, String date, String level, String msg) {
        synchronized (XLog.class) {
            try (FileWriter fw = new FileWriter(file, true);
                    PrintWriter pw = new PrintWriter(fw, true);) {
                pw.print(date);
                pw.println(level);
                pw.println(msg);
                pw.flush();
            } catch (Exception e) {
                Log.w(TAG, "writeLocalLog Error:" + e.getMessage());
            }
        }
    }

    /**
     * 获取堆栈的方法名
     */
    private static String getStackTrace(int index) {
        StackTraceElement[] ses = Thread.currentThread().getStackTrace();

        if (index > -1 && index < ses.length) {
            return ses[index].toString();
        } else {
            return "OutOfRange: " + index + " for " + ses.length + "\n";
        }
    }

    private static String getCurrentDate() { /* 获取当前日期, 按小时存 */
        Date date = new Date(System.currentTimeMillis());
        return getSafeDateFormat("yyyy_MM_dd_HH").format(date);
    }

    public static String getCurrentFormattedTime() { /* 获取当前时间 */
        Date date = new Date(System.currentTimeMillis());
        return getSafeDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static int getTaskSize() {
        return sWorkQueue == null ? 0 : sWorkQueue.size();
    }

    public static void enableShowLog(boolean enable) {
        sShowLog = enable;
    }

    public static boolean isShowLog() {
        return sShowLog;
    }

    public static void enableSdkLog(boolean enable) {
        sShowSDKLog = enable;
        if (enable) {
            LLLog.setLogLs(new ILog());
        } else {
            LLLog.setLogLs(null);
        }
    }

    public static boolean isShowSDKLog() {
        return sShowSDKLog;
    }

    public static void enableSaveLog(boolean enable) {
        sSaveLogToFile = enable;
        createEx();
    }

    public static boolean isSaveLogToFile() {
        return sSaveLogToFile;
    }

    private static void createEx() {
        if (sSaveLogToFile && sExecutor == null) {
            synchronized (XLog.class) {
                if (sExecutor == null) {
                    sWorkQueue = new LinkedBlockingQueue<>(9999);
                    sExecutor = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, sWorkQueue);

                    sLocalLogDir = FileUtils.getSaveLogDir(sContext);
                    System.out.println("LocalLogDir:" + sLocalLogDir);
                    boolean mkdirs = sLocalLogDir.mkdirs();
                    if (!mkdirs) {
                        deleteOldestFile();
                    }
                }
            }
        }
    }

    public static void showStackTrace() {
        if (!sShowLog) {
            return;
        }
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : trace) {
            sb.append(ste).append("\n");
        }
        i(sb.append("StackTrace->").toString(), 5);
    }

    /**
     * 跳转到获取权限的activity
     *
     * @param activity 当前的activity
     */
    public static void startPermissionActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }

    public static class ILog implements LLLog.OnLogL {

        @Override
        public void onLogI(String s) {
            XLog.i("LLLog->" + s, 6);
        }

        @Override
        public void onLogW(String s) {
            XLog.w("LLLog->" + s, 6);
        }

        @Override
        public void onLogE(String s) {
            XLog.e("LLLog->" + s, 6);
        }
    }
}