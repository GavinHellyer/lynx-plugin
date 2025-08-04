package com.ultimafurniture.lynx.bean;

import androidx.annotation.NonNull;

import com.payne.reader.bean.receive.OperationTag;

/**
 * @author naz
 *         Date 2020/4/3
 */
public class OperationTagBean {
    private OperationTag tagBean;
    private int times;

    public OperationTagBean(@NonNull OperationTag bean) {
        this.times = 0;
        setTagBean(bean);
    }

    public void setTagBean(OperationTag bean) {
        this.times++;
        this.tagBean = bean;
    }

    public void addTimes() {
        this.times++;
    }

    public String getEpc() {
        return tagBean.getStrEpc();
    }

    public String getPc() {
        return tagBean.getStrPc();
    }

    public String getCrc() {
        return tagBean.getStrCrc();
    }

    public String getData() {
        return tagBean.getStrData();
    }

    public int getDataLen() {
        return tagBean.getDataLen();
    }

    public int getAntId() {
        return tagBean.getAntId();
    }

    public String getTimes() {
        return String.valueOf(times);
    }

    public int getTagCount() {
        return tagBean.getTagCount();
    }

    public String getFreq() {
        return tagBean.getFreq();
    }

    public int getRssi() {
        return tagBean.getRssi();
    }
}
