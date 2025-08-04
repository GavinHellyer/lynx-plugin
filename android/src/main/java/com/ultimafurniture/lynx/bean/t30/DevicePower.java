package com.ultimafurniture.lynx.bean.t30;

import com.payne.reader.bean.receive.Success;

/**
 * @author naz
 *         Date 2020/8/6
 */
public class DevicePower extends Success {

    /**
     * T 30 equipment power (0-100)
     */
    private int devicePower;

    public int getDevicePower() {
        return devicePower;
    }

    public void setDevicePower(int devicePower) {
        this.devicePower = devicePower;
    }
}
