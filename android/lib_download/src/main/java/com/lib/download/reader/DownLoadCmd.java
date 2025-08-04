package com.lib.download.reader;

import java.lang.reflect.Field;

public class DownLoadCmd {
    /**
     * 读取loader版本和其他信息
     */
    public static final byte CMD_BL_GET_VERSION = 0x00;
    /**
     * 软件复位读写器
     */
    public static final byte CMD_BL_SOFTWARE_RESET = 0x01;
    /**
     * 读写器flash固件下载
     */
    public static final byte CMD_BL_FLASH_DOWNLOAD = 0x02;
    /**
     * flash固件crc32校验
     */
    public static final byte CMD_BL_CHECK_CRC = 0x03;
    /**
     * Bootloader自我更新
     */
    public static final byte CMD_BL_LOADER_UPDATE = 0x04;

    public static final byte CMD_E710_GET_VERSION = 0x30;
    public static final byte CMD_E710_FW_DOWNLOAD = 0x31;

    public static String getNameForCmd(byte cmd) {
        String result = "unknown cmd";

        try {
            Field[] fields = DownLoadCmd.class.getDeclaredFields();
            Field[] var3 = fields;
            int var4 = fields.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Field field = var3[var5];
                field.setAccessible(true);
                byte b = (Byte) field.get((Object) null);
                if (b == cmd) {
                    result = field.getName();
                    break;
                }
            }
        } catch (Throwable var8) {
            var8.printStackTrace();
        }

        return result;
    }
}
