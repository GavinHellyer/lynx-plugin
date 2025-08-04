package com.ultimafurniture.lynx.util;

import com.ultimafurniture.lynx.R;

public class BatteryUtils {
    /* 获取设备电量图片 */
    public static int getPowerPic(int powerLevel) {
        if (powerLevel == 0) {
            return R.mipmap.device_battery_10;
        } else if (powerLevel == 1) {
            return R.mipmap.device_battery_20;
        } else if (powerLevel == 2) {
            return R.mipmap.device_battery_30;
        } else if (powerLevel == 3) {
            return R.mipmap.device_battery_40;
        } else if (powerLevel == 4) {
            return R.mipmap.device_battery_50;
        } else if (powerLevel == 5) {
            return R.mipmap.device_battery_60;
        } else if (powerLevel == 6) {
            return R.mipmap.device_battery_70;
        } else if (powerLevel == 7) {
            return R.mipmap.device_battery_80;
        } else if (powerLevel == 8) {
            return R.mipmap.device_battery_90;
        } else {
            return R.mipmap.device_battery_100;
        }
    }
}
