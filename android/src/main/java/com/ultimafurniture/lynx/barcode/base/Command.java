package com.ultimafurniture.lynx.barcode.base;

/**
 * Created by lei.li on 2016-12-06.
 * The command of Scanner.You can get more from manual of scanner.
 */

public class Command {
    public static final String OPEN_CMD_SET_CODE = "NLS0006010;";
    public static final String CLOSE_CMD_SET_CODE = "NLS0006000;";
    public static final byte[] QUERY_CMD_SEND_PREFIX = { 0x7E, 0x00 };
    public static final byte[] QUERY_CMD_SEND_TYPE = { 0x33 };
    public static final byte[] QUERY_CMD_RETURN_PREFIX = { 0x02, 0x00 };
    public static final byte[] QUERY_CMD_RETURN_TYPE = { 0x34 };
    public static final String DEFAULTE_FACTORY = "NLS0006010;NLS0001000;NLS0006000;";
    public static final String SET_VALUE_SYMBOL = "=";
    public static final String CMD_TRAIL = ";";
    public static final String SET_CMD_PREFIX = "NLS";
    public static final String PREFIX_CUSTOM_SET_CODE = "0300000";
    public static final String SUFFIX_CUSTOM_SET_CODE = "0301000";
    public static final String ENABLE_APPEND_PRE_SUFFIX = "0311010";
    public static final String CODE_ID_DISABLE = "0307000";
    public static final String AIM_ID_DISABLE = "0308000";
    public static final String TERMINAL_SYMBOL_DISABLE = "0309000";

    public static final byte CMD_SET_SUCCESS = 0x06;
    public static final byte CMD_SET_FAILD = 0x15;
    public static final byte ASK = '?';
    public static final byte REPLY = '!';

    /**
     * query instructions;
     */
    public static final byte[] VERSION_INFORMATION = { 0x47 };
    public static final byte[] SERIAL_NO = { 0x48, 0x30, 0x33, 0x30 };
    public static final byte[] ESN = { 0x48, 0x30, 0x32, 0x30 };

    public static final byte[] DATA_OF_PRODUCTION = { 0x48, 0x30, 0x34, 0x30 };

    /**
     * 可设置波特率.
     */
    public static final String[] BAUDRATE = {
            "0100000", "0100010", "0100020", "0100030", "0100040", "0100050", "0100060", "0100070", "0100080"
    };
}
