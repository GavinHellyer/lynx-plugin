package com.ultimafurniture.lynx.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;

/**
 * @author gpenghui
 * @date 2018/5/15
 */

public class StartActivityUtils {
    /**
     * 跳转到新的activity
     *
     * @param activity    当前activity
     * @param targetClass 新的activity的class
     */
    public static void startActivity(Activity activity, @NonNull Class<?> targetClass) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, targetClass);
        activity.startActivity(intent);
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
}
