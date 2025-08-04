package com.ultimafurniture.lynx.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.IBinder;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * @author naz
 *         Date 2020/4/7
 */
public class InputUtils {
    public static void hideInputMethod(Activity activity) {
        View v = activity.getCurrentFocus();
        InputUtils.hideInputMethod(activity, v);
    }

    /**
     * 隐藏软键盘
     *
     * @param context Context
     * @param v       View
     * @return 是否隐藏成功
     */
    public static Boolean hideInputMethod(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && v != null) {
            IBinder windowToken = v.getWindowToken();
            return imm.hideSoftInputFromWindow(windowToken, 0);
        }
        return false;
    }

    /**
     * 判断当前点击屏幕的地方是否是软键盘
     *
     * @param v     View
     * @param event MotionEvent
     * @return true or false
     */
    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if ((v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            v.getLocationInWindow(leftTop);
            int left = leftTop[0],
                    top = leftTop[1],
                    right = left + v.getWidth(),
                    bottom = top + v.getHeight();
            // 保留点击EditText的事件
            float eventX = event.getX();
            float eventY = event.getY();
            return !(eventX > left && eventX < right && eventY > top && eventY < bottom);
        }
        return false;
    }

    public static byte parseByte(EditText editText, int radix) {
        Editable text = editText.getText();
        if (text == null) {
            editText.setError("!");
            throw new IllegalArgumentException("text is null");
        }
        String str = text.toString().trim();
        if (str.length() == 0) {
            editText.setError("!");
            throw new IllegalArgumentException("str is empty");
        }
        return (byte) Integer.parseInt(str, radix);
    }

    public static int parseInt(EditText editText, int radix) throws Exception {
        Editable text = editText.getText();
        if (text == null) {
            editText.setError("!");
            throw new Exception("text is null");
        }
        String str = text.toString().trim();
        if (str.length() == 0) {
            editText.setError("!");
            throw new Exception("str is empty");
        }
        return Integer.parseInt(str, radix);
    }
}
