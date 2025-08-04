package com.ultimafurniture.lynx.bean.t30;

import com.payne.reader.bean.receive.Success;

/**
 * @author naz
 *         Date 2020/8/7
 */
public class TriggerKey extends Success {

    /**
     * 是否使能trigger
     */
    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
