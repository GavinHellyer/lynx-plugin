package com.ultimafurniture.lynx.util;

import androidx.lifecycle.MutableLiveData;

/**
 * 描述：
 * <p>
 * <p>
 * Create by FengLing
 */
public class LiveDataBus {
    public static final int NONE = 0;
    public static final int BUS_SHOW_ACCESS = 1;
    public static final int BUS_SHOW_FIND = 2;
    private final MutableLiveData<LiveDataBusBean> mLiveData = new MutableLiveData<>();

    private LiveDataBus() {
    }

    private static class Inner {
        private static final LiveDataBus INSTANCE = new LiveDataBus();
    }

    public static LiveDataBus getInstance() {
        return Inner.INSTANCE;
    }

    public MutableLiveData<LiveDataBusBean> getLiveData() {
        return Inner.INSTANCE.mLiveData;
    }

    public void postValue(int cmd, String comment) {
        postValue(cmd, comment, null);
    }

    public void postValue(int cmd, String comment, Object obj) {
        mLiveData.postValue(new LiveDataBusBean(cmd, comment, obj));
    }

    public static class LiveDataBusBean {
        public int mCmd;
        public String comment;
        public Object obj;

        public LiveDataBusBean(int cmd, String comment, Object obj) {
            this.mCmd = cmd;
            this.comment = comment;
            this.obj = obj;
        }

        @Override
        public String toString() {
            return "LiveDataBusBean{" +
                    "cmd=" + mCmd +
                    "comment=" + comment +
                    "obj=" + obj +
                    "}";
        }
    }
}