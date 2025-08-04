package com.lib.download.enu;

public enum DownloadFwCheckType {
    /**
     * 校验阶段
     */
    RUN_BOOTLOADER((byte) 0x00),
    RUN_FIRMWARE((byte) 0x01),
    BACKUP_BOOTLOADER((byte) 0x02),
    BACKUP_FIRMWARE((byte) 0x03);

    private final byte value;

    DownloadFwCheckType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
