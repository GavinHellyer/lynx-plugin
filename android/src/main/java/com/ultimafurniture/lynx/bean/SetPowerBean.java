package com.ultimafurniture.lynx.bean;

import com.ultimafurniture.lynx.GlobalCfg;

/**
 * @author naz
 *         Date 2021/3/11
 */
public class SetPowerBean {
    private int power = -1;

    public SetPowerBean() {

    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public static boolean isValid(int power) {
        return power > -1 && power <= GlobalCfg.get().getMaxOutPower();
    }

    @Override
    public String toString() {
        return "SetPowerBean{" +
                "power=" + power +
                '}';
    }
}
