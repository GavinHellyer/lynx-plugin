package com.ultimafurniture.lynx.bean;

import com.payne.reader.bean.config.FastTidType;
import com.payne.reader.bean.config.Session;
import com.payne.reader.bean.config.Target;

/**
 * @author naz
 *         Date 2020/4/10
 */
public class CustomSessionBean {
    /**
     * Customized Session
     */
    private Session session;
    /**
     * Session custom tag type
     */
    private Target target;
    /**
     * inventory times for each tag
     */
    private byte repeat;
    /**
     * Impinj Monza tag type
     */
    private FastTidType fastType;

    public CustomSessionBean() {
        // normal S1
        this.session = Session.S0;
        // normal A
        this.target = Target.A;
        // normal 1
        this.repeat = 1;
        // normal 0x00 Disable
        this.fastType = FastTidType.DISABLE;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public byte getRepeat() {
        return repeat;
    }

    public void setRepeat(byte repeat) {
        this.repeat = repeat;
    }

    public FastTidType getFastType() {
        return fastType;
    }

    public void setFastType(FastTidType fastType) {
        this.fastType = fastType;
    }
}
