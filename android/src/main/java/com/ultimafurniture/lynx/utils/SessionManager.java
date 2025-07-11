package com.ultimafurniture.lynx.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SessionManager {

    private static final String TAG = "SessionManager";

    private Context context;
    private SharedPreferences preferences;
    private static SessionManager INSTANCE;
    private String appName = "";

    private SessionManager(Context context) {
        this.context = context;
        appName = context.getPackageName();
        preferences = context.getSharedPreferences(appName + "_session", Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new SessionManager(context);
        return INSTANCE;
    }

    // call this method from Application class for initialization
    public static void init(Context context) {
        INSTANCE = new SessionManager(context);
    }

    public void setStringValue(String key, String stringValue) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, stringValue);
        editor.apply();
    }

    public String getStringValue(String key) {
        return preferences.getString(key, "");
    }

    public void setIntValue(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getIntValue(String key) {
        return preferences.getInt(key, 0);
    }

    public void setBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBooleanValue(String key) {
        return preferences.getBoolean(key, false);
    }

    public void setStringSet(String key, Set<String> strings) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(key, strings);
        editor.apply();
    }

    public Set<String> getStringSet(String key) {
        return preferences.getStringSet(key, new HashSet<>());
    }

    public void setLongValue(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLongValue(String key) {
        return preferences.getLong(key, 0);
    }

    public void setFloatValue(String key, float value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public float getFloatValue(String key) {
        return preferences.getFloat(key, 0);
    }
}
