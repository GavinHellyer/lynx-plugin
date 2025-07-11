package com.ultimafurniture.lynx.models;

import com.rfid.rxobserver.bean.RXOperationTag;

public class OperationTag extends RXOperationTag {
    public String strPC = "";
    public String strCRC = "";
    public String strEPC = "";
    public String strData = "";
    public int nDataLen = 0;
    public byte btAntId = 0;
    public int nOperateCount = 0;
    public byte cmd = 0;

    public OperationTag() {
    }

    public OperationTag(RXOperationTag operationTag) {
        strCRC = operationTag.strCRC;
        strPC = operationTag.strPC;
        strEPC = operationTag.strEPC;
        strData = operationTag.strData;
        ;
        nDataLen = operationTag.nDataLen;
        btAntId = operationTag.btAntId;
        nOperateCount = operationTag.nOperateCount;
        cmd = operationTag.cmd;
    }
}
