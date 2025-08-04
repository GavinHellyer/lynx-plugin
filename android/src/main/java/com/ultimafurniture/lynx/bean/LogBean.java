package com.ultimafurniture.lynx.bean;

/**
 * @author naz
 *         Date 2020/7/1
 */
public class LogBean {
    private String msg;
    private boolean isSuccess;
    private long time;

    public LogBean(String msg, boolean isSuccess) {
        this.msg = msg;
        this.isSuccess = isSuccess;
        this.time = System.currentTimeMillis();
    }

    public String getMsg() {
        return msg;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public long getTime() {
        return time;
    }
}
