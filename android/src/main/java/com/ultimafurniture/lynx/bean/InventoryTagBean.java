package com.ultimafurniture.lynx.bean;

import android.os.Bundle;

import com.payne.reader.bean.receive.InventoryTag;

import java.io.Serializable;
import java.util.Formatter;

/**
 * @author naz
 *         Date 2020/4/3
 */
public class InventoryTagBean implements Serializable {
    private InventoryTag inventoryTag;
    private Integer mP;
    private Integer mTimes;
    private String mTimeStr;
    private Formatter mFormatter;
    private StringBuilder mSb;

    public InventoryTagBean(InventoryTag bean, int p) {
        inventoryTag = bean;
        mP = p;
        mTimes = 1;
        mTimeStr = "1";
        mSb = new StringBuilder();
        mFormatter = new Formatter(mSb);
    }

    public void addTimes() {
        mTimes++;
        mTimeStr = String.valueOf(mTimes);
    }

    public void setInventoryTag(InventoryTag tag) {
        this.inventoryTag = tag;
    }

    public String getEpc() {
        return inventoryTag.getEpc();
    }

    public String getPc() {
        return inventoryTag.getPc();
    }

    public Integer getTimes() {
        return mTimes;
    }

    public String getTimesStr() {
        return mTimeStr;
    }

    public String getRssi() {
        return inventoryTag.getRssi() + "dBm";
    }

    public int getAntId() {
        return inventoryTag.getAntId();
    }

    public String getFreq() {
        return inventoryTag.getFreq();
    }

    public String getPhase() {
        mSb.setLength(0);
        short phase = (short) inventoryTag.getPhase();
        return mFormatter.format("%2X", phase).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InventoryTagBean)) {
            return false;
        }

        InventoryTagBean that = (InventoryTagBean) o;

        if (inventoryTag != null) {
            return inventoryTag.getEpc().equals(that.inventoryTag.getEpc());
        }
        return false;
    }

    public Integer getPosition() {
        return mP;
    }
}