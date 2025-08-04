package com.ultimafurniture.lynx.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.ultimafurniture.lynx.bean.type.Key;
import com.ultimafurniture.lynx.bean.type.LanguageType;
import com.orhanobut.hawk.Hawk;

import java.util.Locale;

/**
 * @author naz
 *         Date 2020/4/14
 */
public class LanguageUtils {

    /**
     * 设置语言
     *
     * @param type 预览类型 如{@link Key#LANGUAGE_TYPE_FOLLOW_SYSTEM}..
     */
    public static void putLanguageType(int type) {
        Hawk.put(Key.LANGUAGE_TYPE, type);
    }

    /**
     * 获取语言类型
     *
     * @return 如 {@link Key#LANGUAGE_TYPE_FOLLOW_SYSTEM}..
     */
    public static int getLanguageType() {
        return Hawk.get(Key.LANGUAGE_TYPE, Key.LANGUAGE_TYPE_FOLLOW_SYSTEM);
    }

    /**
     * 根据之前保存的预览类型获取Language
     *
     * @return String
     */
    public static String getLanguage() {
        int type = getLanguageType();
        if (type == Key.LANGUAGE_TYPE_CHINESE) {
            return LanguageType.CHINESE.getLanguage();
        } else if (type == Key.LANGUAGE_TYPE_ENGLISH) {
            return LanguageType.ENGLISH.getLanguage();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
            } else {
                return Locale.getDefault().getLanguage();
            }
        }
    }

    /**
     * 语言切换
     *
     * @param context  {@link Context}
     * @param language 语言类型
     * @return Context
     */
    public static Context updateLanguage(Context context, String language) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();

        if (Build.VERSION.SDK_INT > 23) {
            LocaleList defaultList = LocaleList.forLanguageTags(language);
            LocaleList.setDefault(defaultList);
            conf.setLocales(defaultList);
            Locale.setDefault(new Locale(language));
            context.createConfigurationContext(conf);
        } else {
            conf.setLocale(new Locale(language));
        }
        res.updateConfiguration(conf, dm);
        return context;
    }
}
