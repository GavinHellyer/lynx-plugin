package com.ultimafurniture.lynx.util;

import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.ultimafurniture.lynx.R;

/**
 *
 */
public class ToastUtils {
    public static Toast makeText(View v, int resStr, int i) {
        String str = XLog.sContext.getString(resStr);
        return makeText(v, str, i);
    }

    public static Toast makeText(View v, String str, int i) {
        Toast toast = Toast.makeText(XLog.sContext, str, i);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 60);
        toast.getView().setBackgroundResource(R.drawable.shape_tip_r5);
        return toast;
    }
}
