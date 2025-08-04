package com.ultimafurniture.lynx.bean.t30;

/**
 * @author gpenghui
 *         date 2018/7/11
 */

public class BleDeviceBean {
    /**
     * 名称
     */
    private String name;
    /**
     * 地址
     */
    private String address;
    /**
     * 信号强度
     */
    private int signal;

    public BleDeviceBean(String name, String address, int signal) {
        this.name = name;
        this.address = address;
        this.signal = signal;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
