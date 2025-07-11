package com.ultimafurniture.lynx.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AppUtils {

    private static final String TAG = "AppUtils";

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'");
    static String format;

    public static String getFormattedTimestamp() {

        try {
            format = simpleDateFormat.format(new Date());
        } catch (Exception e) {
            return "" + format;
        }
        return format;
    }

    // public static Inventory getMatchingInventory(String strEPC, List<Inventory>
    // list) {
    // String formattedEPC = strEPC.replace(" ", "");
    // for (Inventory inventory : list) {
    // if (formattedEPC.equals(inventory.getFormattedEPC())) {
    // return inventory;
    // }
    // }
    // return null;
    // }

    public static String decimalToHex(long value) {
        return Long.toHexString(value).toUpperCase();
    }

    public static String decimalToHex(String value) {
        value = value.replace(" ", "");
        value = value.replace(".", "");

        long l = Long.parseLong(value);
        return Long.toHexString(l).toUpperCase();
    }

    public static String stringToHex(String value) {
        StringBuffer sb = new StringBuffer();
        // Converting string to character array
        char ch[] = value.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            String hexString = Integer.toHexString(ch[i]);
            sb.append(hexString);
        }
        String result = sb.toString();
        return result;
    }

    // Mobiux hex is : 6D 6F 62 69 75 78
    // SO hex is : 53 4F
    private static String epcHeader = "53 4F ";

    public static String generateHexEPC(String value) {

        StringBuffer sb = new StringBuffer();
        String hexString = numberToHex(value);

        int zeroRequired = 20 - hexString.length();
        int zeroAddedCount = 0;

        while (zeroRequired > 0) {

            sb.append(0);
            zeroRequired--;
            zeroAddedCount++;

            if (zeroAddedCount % 2 == 0) {
                sb.append(" ");
            }
        }

        hexString = sb + hexString;
        hexString = epcHeader + hexString;
        return hexString.toUpperCase();
    }

    public static String numberToHex(String number) {
        if (!isNumber(number)) {
            throw new RuntimeException("value should be number only");
        }

        try {
            return Long.toHexString(Long.parseLong(number));

        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public static boolean isNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int byteArrayToInt(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
        }
        return result;
    }

    public static String getFormattedTimestampUpToSeconds() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = null;
        try {
            format = simpleDateFormat.format(new Date());
        } catch (Exception e) {
            return "" + format;
        }
        return format;
    }

    public static String getFormattedEPC(String epc) {
        return epc.replace(" ", "");
    }

    public static List<ActivityManager.RunningTaskInfo> getRunningApps(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> recentTasks = Objects.requireNonNull(activityManager)
                .getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo taskInfo : recentTasks) {
            Log.i(TAG, "getRunningApps: " + taskInfo.baseActivity.getPackageName());
        }

        return recentTasks;
    }
}
