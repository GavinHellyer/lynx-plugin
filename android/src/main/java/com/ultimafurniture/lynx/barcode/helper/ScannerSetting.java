package com.ultimafurniture.lynx.barcode.helper;

import java.util.HashMap;
import java.util.Map;

import com.ultimafurniture.lynx.barcode.base.Command;

/**
 * Created by lei.li on 2016-12-05.
 */

public class ScannerSetting {

    private static final String TAG = "ScannerSetting";

    private static final ScannerSetting mScannerSetting = new ScannerSetting();

    // default prefix and suffix;
    public static final String DTAT_PREFIX = "0x02";
    public static final String DTAT_SUFFIX = "0x03";

    /**
     * The default constructor
     */
    private ScannerSetting() {
    }

    /**
     * Get the ScannerSetting object;
     *
     * @return the ScannerSetting object;
     */
    public static ScannerSetting newInstance() {
        return mScannerSetting;
    }

    /**
     * set default setting
     *
     * @return if ture the setting success,or failure.
     * @throws Exception
     */
    public boolean defaultSettings() throws Exception {
        Map<byte[], byte[]> map = new HashMap<>();
        // map.put(Command.BAUDRATE[Command.BAUDRATE.length - 1].getBytes(),null);
        map.put(Command.ENABLE_APPEND_PRE_SUFFIX.getBytes(), null);
        map.put(Command.PREFIX_CUSTOM_SET_CODE.getBytes(), DTAT_PREFIX.getBytes());
        map.put(Command.SUFFIX_CUSTOM_SET_CODE.getBytes(), DTAT_SUFFIX.getBytes());
        ReaderHelper.getDefaultHelper().getReader().setScannerAttr(map);

        ReaderHelper.getDefaultHelper().getReader().setScannerAttr(Command.CODE_ID_DISABLE.getBytes(), null);
        ReaderHelper.getDefaultHelper().getReader().setScannerAttr(Command.AIM_ID_DISABLE.getBytes(), null);
        Thread.sleep(300);
        ReaderHelper.getDefaultHelper().getReader().setScannerAttr(Command.TERMINAL_SYMBOL_DISABLE.getBytes(), null);
        return true;
    }
}
