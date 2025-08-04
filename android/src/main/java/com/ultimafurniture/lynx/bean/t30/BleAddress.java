package com.ultimafurniture.lynx.bean.t30;

import com.payne.reader.bean.receive.Success;

/**
 * @author naz
 *         Date 2020/8/6
 */
public class BleAddress extends Success {

    /**
     * Bluetooth MAC address
     */
    private String macAddress;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
