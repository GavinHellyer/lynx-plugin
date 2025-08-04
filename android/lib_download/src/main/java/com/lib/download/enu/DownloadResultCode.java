package com.lib.download.enu;

public enum DownloadResultCode {
    /**
     * 返回结果
     */
    SUCCESS((byte) 0x10),
    FAIL((byte) 0x11),
    DOWNLOAD_START_ERROR((byte) 0x60),
    DOWNLOAD_SEGMENT_ERROR((byte) 0x61),
    DOWNLOAD_FINISH_ERROR((byte) 0x62);

    private final byte value;

    DownloadResultCode(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static DownloadResultCode valueOf(byte value) {
        if (value == SUCCESS.value) {
            return SUCCESS;
        } else if (value == DOWNLOAD_START_ERROR.value) {
            return DOWNLOAD_START_ERROR;
        } else if (value == DOWNLOAD_SEGMENT_ERROR.value) {
            return DOWNLOAD_SEGMENT_ERROR;
        } else if (value == DOWNLOAD_FINISH_ERROR.value) {
            return DOWNLOAD_FINISH_ERROR;
        }
        return FAIL;
    }
}
