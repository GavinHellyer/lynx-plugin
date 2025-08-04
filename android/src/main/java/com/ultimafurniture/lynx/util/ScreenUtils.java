package com.ultimafurniture.lynx.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

/**
 * @author gpenghui
 */

public class ScreenUtils {
    private ScreenUtils() {
    }

    /**
     * dp转换成px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转换成dp
     */
    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp转换成px
     */
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * px转换成sp
     */
    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 获取手机是否是横屏
     *
     * @param activity activity
     * @return 是否是横屏
     */
    public static boolean isLandscape(Activity activity) {
        // 获取设置的配置信息
        Configuration mConfiguration = activity.getResources().getConfiguration();
        // 获取屏幕方向
        int ori = mConfiguration.orientation;
        return ori == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
