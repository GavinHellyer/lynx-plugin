package com.ultimafurniture.lynx.bean.t30;

import com.payne.reader.bean.receive.Success;

/**
 * @author naz
 *         Date 2020/8/6
 */
public class ModuleStatus extends Success {
    private byte moduleType;
    /**
     * Whether the module is on
     */
    private boolean isEnable;

    public byte getModuleType() {
        return moduleType;
    }

    public void setModuleType(byte moduleType) {
        this.moduleType = moduleType;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
