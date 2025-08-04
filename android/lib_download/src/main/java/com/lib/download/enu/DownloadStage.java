package com.lib.download.enu;

public enum DownloadStage {
    /**
     * 下载阶段
     */
    START_DOWNLOAD((byte) 0x00),
    DOWNLOADING((byte) 0x01),
    END_DOWNLOAD((byte) 0x02);

    private final byte value;

    DownloadStage(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
