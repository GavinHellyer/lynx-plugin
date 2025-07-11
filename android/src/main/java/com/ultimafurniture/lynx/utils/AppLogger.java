package com.ultimafurniture.lynx.utils;

public class AppLogger {

    private static final String TAG = "AppLogger";
    private boolean isDebug = false;

    private static AppLogger logger;
    private FileUtils fileUtils;

    private AppLogger() {
        fileUtils = new FileUtils();
    }

    public static AppLogger getInstance() {
        if (logger == null)
            logger = new AppLogger();
        return logger;
    }

    public void i(String tag, String message) {
        fileUtils.log("INFO : " + tag + "\t" + message);
    }

    public void e(String tag, String message) {
        fileUtils.log("ERROR : " + tag + "\t" + message);
    }

    public void d(String tag, String message) {
        fileUtils.log("DEBUG : " + tag + "\t" + message);
    }

    public void w(String tag, String message) {
        fileUtils.log("WARNING : " + tag + "\t" + message);
    }

}
