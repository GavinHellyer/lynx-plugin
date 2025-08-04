package com.lib.download.bean;

import com.lib.download.enu.DownloadChipType;
import com.lib.download.enu.DownloadResultCode;

public class DownloadVersionResult {
    private DownloadResultCode resultCode;
    private DownloadChipType chipType = DownloadChipType.UNKNOWN;
    private String version;
    private String bootVersion;
    private String buildTime;
    private Integer encryptedMode;

    public DownloadVersionResult(DownloadResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public DownloadVersionResult(DownloadChipType ct, String version, String bootVersion, String buildTime, Integer encryptedMode) {
        this.resultCode = DownloadResultCode.SUCCESS;
        if (chipType != null) {
            this.chipType = ct;
        }
        this.version = version;
        this.bootVersion = bootVersion;
        this.buildTime = buildTime;
        this.encryptedMode = encryptedMode;
    }

    public DownloadResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(DownloadResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public DownloadChipType getChipType() {
        return chipType;
    }

    public void setChipType(DownloadChipType chipType) {
        this.chipType = chipType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBootVersion() {
        return bootVersion;
    }

    public void setBootVersion(String bootVersion) {
        this.bootVersion = bootVersion;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public Integer getEncryptedMode() {
        return encryptedMode;
    }

    @Override
    public String toString() {
        return "VersionResult{" +
                "resultCode=" + resultCode + "\n" +
                ", chipType=" + chipType + "\n" +
                ", version=" + version + "\n" +
                ", bootVersion=" + bootVersion + "\n" +
                ", buildTime=" + buildTime + "\n" +
                ", encryptedMode=" + encryptedMode + "\n" +
                "}";
    }
}
