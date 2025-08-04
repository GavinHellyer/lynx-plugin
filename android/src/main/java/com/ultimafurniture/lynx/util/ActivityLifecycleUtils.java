package com.ultimafurniture.lynx.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.LinkedList;

/**
 * @author FengLing
 *         Created on 2016-7-22 21:59
 *         Activity Lifecycle的相关工具类
 */
public final class ActivityLifecycleUtils {
    /**
     * 使用 LinkedList 保存每个 Activity , 增删 速度快
     */
    public static LinkedList<Activity> sList;
    /**
     * 当前顶层动的activity
     */
    private static Activity sActivity;
    private static InnerCallbacks sCallback;

    // 工具类不允许实例化
    private ActivityLifecycleUtils() {
        throw new NullPointerException("来个空指针玩玩~");
    }

    public static void init(Application application) {
        if (sList != null) {
            return;
        }
        sList = new LinkedList<>();
        sCallback = new InnerCallbacks();
        application.registerActivityLifecycleCallbacks(sCallback);
    }

    public static Activity getTopActivity() {
        return sActivity;
    }

    public static Fragment getTopFragment() {
        return sCallback.mFLC.mF;
    }

    public static void finishOthers(Activity retainActivity) {
        for (Activity activity : sList) {
            if (activity != retainActivity) {
                activity.finish();
            }
        }
    }

    /**
     * 退出整个应用程序
     */
    public static void finishAll() {
        if (sList != null) {
            int size = sList.size();
            for (int i = 0; i < size; i++) {
                Activity activity = sList.pollFirst();
                if (activity != null) {
                    activity.finish();
                }
            }
            XLog.i("finishAll->" + sList);
        }
    }

    /**
     * 程序转入后台, 返回桌面
     *
     * @param context 当前打开的Activity
     */
    public static void gotoDesktop(Context context) {
        XLog.i("回桌面", 5);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
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

    private static class InnerCallbacks implements Application.ActivityLifecycleCallbacks {
        private FLC mFLC = new FLC();

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            String msg = "onActivityCreated:" + activity;
            sList.add(activity);
            if (activity instanceof FragmentActivity) {
                FragmentActivity fa = (FragmentActivity) activity;
                fa.getSupportFragmentManager().registerFragmentLifecycleCallbacks(mFLC, true);
                msg += ", 注册了Fragment监听";
            }
            XLog.i(msg);
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            XLog.i("onActivityResumed:" + activity);
            sActivity = activity;
        }

        @Override
        public void onActivityPaused(Activity activity) {
            XLog.i("onActivityPaused:" + activity);
            if (activity == sActivity) {
                sActivity = null;
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            String msg = "onActivityDestroyed:" + activity;
            sList.remove(activity);
            if (activity instanceof FragmentActivity) {
                FragmentActivity fa = (FragmentActivity) activity;
                fa.getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(mFLC);
                msg += ", 注销了Fragment监听";
            }
            XLog.i(msg);
        }
    }

    private static class FLC extends FragmentManager.FragmentLifecycleCallbacks {
        private Fragment mF;

        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            XLog.i("f-->" + f);
            mF = f;
        }

        @Override
        public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
            XLog.i("f-->" + f);
            if (f == mF) {
                mF = null;
            }
        }

        @Override
        public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            XLog.i("f-->" + f);
        }
    }
}