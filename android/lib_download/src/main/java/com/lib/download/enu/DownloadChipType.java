package com.lib.download.enu;

public enum DownloadChipType {
    /**
     * 芯片类型
     */
    UNKNOWN((byte) -0x01),
    // CHIP_IBAT_2000((byte) 0x00),
    CHIP_R2000((byte) (0x00)),
    CHIP_E710((byte) 0x01),
    CHIP_RODINBELL((byte) 0x02),
    CHIP_TM600((byte) 0x03),
    CHIP_FDW((byte) 0x04),
    CHIP_PRINTER((byte) 0x80);

    private final byte value;

    DownloadChipType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static DownloadChipType valueOf(byte value) {
        DownloadChipType[] values = DownloadChipType.values();
        for (DownloadChipType chipType : values) {
            if (chipType.value == value) {
                return chipType;
            }
        }
        return UNKNOWN;
    }
}
