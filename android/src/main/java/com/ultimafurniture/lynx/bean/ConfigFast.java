package com.ultimafurniture.lynx.bean;

import java.util.List;

/**
 * @author naz
 *         Date 2021/1/26
 */
public class ConfigFast {
    private List<String> channels;
    private int selectId;
    private int repeat;

    public ConfigFast(List<String> channels, int selectId, int repeat) {
        this.channels = channels;
        this.selectId = selectId;
        this.repeat = repeat;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public int getSelectId() {
        return selectId;
    }

    public void setSelectId(int selectId) {
        this.selectId = selectId;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }
}
