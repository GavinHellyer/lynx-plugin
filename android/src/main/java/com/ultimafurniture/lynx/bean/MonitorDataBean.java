package com.ultimafurniture.lynx.bean;

/**
 * @author naz
 *         Date 2020/4/27
 */
public class MonitorDataBean {
    /**
     * 当前数据
     */
    private String data;
    /**
     * 收发当前数据的时间
     */
    private long currentTime;
    /**
     * 是否是发送数据（不是发送就是接收）
     */
    private boolean isSend;

    public MonitorDataBean(String data, boolean isSend) {
        this.data = data + "";
        this.isSend = isSend;
        this.currentTime = System.currentTimeMillis();
    }

    public String getData() {
        return data;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public boolean isSend() {
        return isSend;
    }
}
