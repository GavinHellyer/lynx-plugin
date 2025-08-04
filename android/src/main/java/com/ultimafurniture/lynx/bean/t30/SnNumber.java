package com.ultimafurniture.lynx.bean.t30;

import com.payne.reader.bean.receive.Success;

/**
 * @author naz
 *         Date 2020/8/6
 */
public class SnNumber extends Success {

    /**
     * Interface board SN number
     */
    private String interfaceBoardSn;

    public String getInterfaceBoardSn() {
        return interfaceBoardSn;
    }

    public void setInterfaceBoardSn(String interfaceBoardSn) {
        this.interfaceBoardSn = interfaceBoardSn;
    }
}
