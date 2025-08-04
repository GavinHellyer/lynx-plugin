package com.lib.download.bean;

import com.lib.download.enu.DownloadResultCode;

public class DownloadCheckCrcResult {
    private DownloadResultCode resultCode;
    private int crcCheck;
    private int crcStored;

    public DownloadCheckCrcResult(DownloadResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public DownloadCheckCrcResult(int crcCheck, int crcStored) {
        this.resultCode = DownloadResultCode.SUCCESS;
        this.crcCheck = crcCheck;
        this.crcStored = crcStored;
    }

    public DownloadResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(DownloadResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public int getCrcCheck() {
        return crcCheck;
    }

    public void setCrcCheck(int crcCheck) {
        this.crcCheck = crcCheck;
    }

    public int getCrcStored() {
        return crcStored;
    }

    public void setCrcStored(int crcStored) {
        this.crcStored = crcStored;
    }
}
