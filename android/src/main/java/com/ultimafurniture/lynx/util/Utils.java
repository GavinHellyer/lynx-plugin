package com.ultimafurniture.lynx.util;

import android.widget.EditText;

import com.payne.reader.bean.config.AntennaCount;

/**
 * @author 一叶丶枫凌
 *         Last Modify on
 *         Desc:
 */
public class Utils {

    public static int parseInt_0_FF(EditText et, int def) {
        return parseInt_0_FF(et, def, "!");
    }

    public static int parseInt_0_FF(EditText et, int def, String errTip) {
        try {
            int value = Integer.parseInt(et.getText().toString(), 16);
            if (value < 0 || value > 0xFF) {
                et.setError("!");
                return def;
            }
            return value;
        } catch (Exception e) {
            et.setError(errTip);
            return def;
        }
    }

    public static int parseInt(EditText et, int def) {
        return parseInt(et, def, "!");
    }

    public static int parseInt(EditText et, int def, String errTip) {
        try {
            String str = et.getText().toString();
            return Integer.parseInt(str);
        } catch (Exception e) {
            et.setError(errTip);
            return def;
        }
    }

    public static float parseFloat(EditText et, float def) {
        try {
            String str = et.getText().toString();
            return Float.parseFloat(str);
        } catch (Exception e) {
            et.setError("!");
            return def;
        }
    }

    public static AntennaCount getAntEnumByIndex(int position) {
        AntennaCount count;
        switch (position) {
            default:
            case 0:
                count = AntennaCount.SINGLE_CHANNEL;
                break;
            case 1:
                count = AntennaCount.FOUR_CHANNELS;
                break;
            case 2:
                count = AntennaCount.EIGHT_CHANNELS;
                break;
            case 3:
                count = AntennaCount.SIXTEEN_CHANNELS;
                break;
        }
        return count;
    }

    public static int getIndexByAntEnum(AntennaCount antennaCount) {
        int index;
        switch (antennaCount) {
            default:
            case SINGLE_CHANNEL:
                index = 0;
                break;
            case FOUR_CHANNELS:
                index = 1;
                break;
            case EIGHT_CHANNELS:
                index = 2;
                break;
            case SIXTEEN_CHANNELS:
                index = 3;
                break;
        }
        return index;
    }

}
