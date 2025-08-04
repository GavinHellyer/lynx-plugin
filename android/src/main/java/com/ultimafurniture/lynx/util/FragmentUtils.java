package com.ultimafurniture.lynx.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

/**
 * @author naz
 *         Date 2020/4/11
 */
public class FragmentUtils {
    /**
     * 获取fragment
     *
     * @param fragmentManager 根据FragmentManager获取Fragment
     * @param fragmentClass   Fragment类名
     * @return Fragment or null;
     */
    public static Fragment getFragment(FragmentManager fragmentManager, Class<?> fragmentClass) {
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.getClass() == fragmentClass) {
                return fragment;
            }
        }
        return null;
    }

    /**
     * 获取可见fragment
     *
     * @param fragmentManager 根据FragmentManager获取Fragment
     * @return Fragment or null;
     */
    public static Fragment getVisibleFragment(FragmentManager fragmentManager) {
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null || fragments.isEmpty()) {
            return null;
        }
        for (Fragment fragment : fragments) {
            if (fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }
}
