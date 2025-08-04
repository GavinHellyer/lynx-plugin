package com.lib.download.enu;

public enum DownloadFwType {
    /**
     * 下载阶段
     */
    UNKNOWN((byte) -0x01),
    BOOTLOADER((byte) 0x00),
    FIRMWARE((byte) 0x01),
    BOOTLOADER_I_RAM((byte) 0x02),
    FIRMWARE_I_RAM((byte) 0x03),
    CHIP((byte) 0x10);

    private final byte value;

    DownloadFwType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static DownloadFwType valueOf(byte value) {
        if (value == BOOTLOADER.value) {
            return BOOTLOADER;
        }
        if (value == FIRMWARE.value) {
            return FIRMWARE;
        }
        if (value == CHIP.value) {
            return CHIP;
        }
        if (value == BOOTLOADER_I_RAM.value) {
            return BOOTLOADER_I_RAM;
        }
        if (value == FIRMWARE_I_RAM.value) {
            return FIRMWARE_I_RAM;
        }
        return UNKNOWN;
    }
}
