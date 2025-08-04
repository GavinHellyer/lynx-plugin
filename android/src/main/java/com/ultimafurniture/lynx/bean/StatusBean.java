package com.ultimafurniture.lynx.bean;

/**
 * @author naz
 *         Date 2020/4/23
 */
public class StatusBean {
    private int status;

    public StatusBean() {
        this.status = 0;
    }

    public void setSnPasswordStatus(boolean ok) {
        int mask = 0x01;
        if (ok) {
            status |= mask;
        } else {
            status &= (~mask);
        }
    }

    public void setSnNumberStatus(boolean ok) {
        int mask = 0x01 << 1;
        if (ok) {
            status |= mask;
        } else {
            status &= (~mask);
        }
    }

    public boolean getSnStatus() {
        return (status & 0x03) == 0x03;
    }

    public void setBeeperRepeatStatus(boolean ok) {
        int mask = 0x01 << 2;
        if (ok) {
            status |= mask;
        } else {
            status &= (~mask);
        }
    }

    public void setBeeperSoundingTimeStatus(boolean ok) {
        int mask = 0x01 << 3;
        if (ok) {
            status |= mask;
        } else {
            status &= (~mask);
        }
    }

    public void setBeeperQuietTimeStatus(boolean ok) {
        int mask = 0x01 << 4;
        if (ok) {
            status |= mask;
        } else {
            status &= (~mask);
        }
    }

    public boolean getBeeperStatus() {
        return ((status >> 2) & 0x07) == 0x07;
    }
}
