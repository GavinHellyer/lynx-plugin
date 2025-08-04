package com.ultimafurniture.lynx.util;

public class FastClickUtils {

    private static long MS = 800;
    private static int sLastButtonId = -1;
    private static long sLastClickTime = 0;

    /**
     * 判断两次点击的间隔，如果小于800，则认为是多次无效点击
     *
     * @return
     */
    public static boolean isFastClick() {
        return isFastClick(-1, MS);
    }

    /**
     * 判断两次点击的间隔，如果小于1000，则认为是多次无效点击
     *
     * @return
     */
    public static boolean isFastClick(int buttonId) {
        return isFastClick(buttonId, MS);
    }

    /**
     * 判断两次点击的间隔，如果小于diff，则认为是多次无效点击
     *
     * @param diff
     * @return
     */
    public static boolean isFastClick(int buttonId, long diff) {
        long time = System.currentTimeMillis();
        long timeD = time - sLastClickTime;
        if (sLastButtonId == buttonId && sLastClickTime > 0 && timeD < diff) {
            XLog.w("短时间内按钮多次触发");
            return true;
        }
        sLastClickTime = time;
        sLastButtonId = buttonId;
        return false;
    }
}