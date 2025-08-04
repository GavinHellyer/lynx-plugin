package com.lib.download.util;

import android.util.Log;

import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.Objects;

public class ArrayUtils {
    /**
     * preset value
     */
    private static final int CRC32_SEED = 0xFFFFFFFF;
    private static final int POLY32 = 0x4C11DB7;
    private static final int[] CRC_TABLE = {
            0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
            0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
            0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
            0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
            0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
            0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
            0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
            0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
            0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
            0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
            0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
            0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
            0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
            0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
            0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
            0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
            0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
            0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
            0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
            0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
            0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
            0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
            0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
            0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
            0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
            0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
            0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
            0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
            0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
            0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
            0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
            0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
    };

    /**
     * Check if bytes is null
     *
     * @param bytes Byte array
     * @return bool
     */
    public static boolean isEmpty(byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }

    /**
     * Merge two byte arrays into a new array
     *
     * @param lBytes byte array
     * @param rBytes byte array
     * @return a new byte array
     */
    public static byte[] mergeBytes(byte[] lBytes, byte[] rBytes) {
        if (lBytes == null) {
            if (rBytes == null) {
                return new byte[0];
            } else {
                return rBytes;
            }
        } else if (rBytes == null) {
            return lBytes;
        }
        byte[] mergeData = new byte[lBytes.length + rBytes.length];
        System.arraycopy(lBytes, 0, mergeData, 0, lBytes.length);
        System.arraycopy(rBytes, 0, mergeData, lBytes.length, rBytes.length);
        return mergeData;
    }

    /**
     * Byte array to hex string
     *
     * @param bytes Byte array
     * @return Hex string
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return bytesToHexString(bytes, 0, bytes.length, " ");
    }

    /**
     * Byte array to hex string
     *
     * @param bytes     Byte array
     * @param fromIndex start position
     * @param len       Conversion length
     * @return Hex string
     */
    public static String bytesToHexString(byte[] bytes, int fromIndex, int len, String splitStr) {
        return bytesToString(bytes, fromIndex, len, true, splitStr);
    }

    /**
     * Byte array to hex string
     *
     * @param bytes     Byte array
     * @param fromIndex start position
     * @param len       Conversion length
     * @param hex       Is it a hexadecimal string
     * @return String
     */
    public static String bytesToString(byte[] bytes, int fromIndex, int len, boolean hex, String splitStr) {
        if (fromIndex >= bytes.length) {
            return "";
        }
        if (fromIndex + len > bytes.length) {
            len = bytes.length - fromIndex;
        }
        int i;
        int endIndex = fromIndex + len;
        String format = hex ? "%02X" : "%03d";
        StringBuilder strResult = new StringBuilder(128);
        Locale locale = Locale.getDefault();
        for (i = fromIndex; i < endIndex; i++) {
            if (i != fromIndex) {
                strResult.append(splitStr);
            }
            String strTemp = String.format(locale, format, (bytes[i] & 0xFF));
            strResult.append(strTemp);
        }
        return strResult.toString();
    }

    /**
     * Convert int to byte array
     *
     * @param dst    Target array
     * @param dstPos Starting subscript
     * @param src    int value
     * @param len    Data length
     */
    public static void intAddToBytes(byte[] dst, int dstPos, int src, int len) {
        intAddToBytes(dst, dstPos, src, len, true);
    }

    /**
     * Convert int to byte array
     *
     * @param dst       Target array
     * @param dstPos    Starting subscript
     * @param src       int value
     * @param len       Data length
     * @param bigEndian Is it big endian?
     */
    public static void intAddToBytes(byte[] dst, int dstPos, int src, int len, boolean bigEndian) {
        if (dst == null) {
            throw new InvalidParameterException("The target array cannot be null");
        }
        if (dstPos < 0 || len < 0) {
            throw new InvalidParameterException("The starting index or length cannot be less than 0");
        }
        if (len > 4) {
            throw new InvalidParameterException("The length cannot be greater than the maximum length of int 4 bytes");
        }
        int end = dstPos + len;
        if (end > dst.length) {
            throw new InvalidParameterException("Length out of bounds");
        }
        for (int i = 0; i < len; i++) {
            int offset;
            if (bigEndian) {
                offset = len - 1 - i;
            } else {
                offset = i;
            }
            dst[i + dstPos] = (byte) (src >> (offset << 3));
        }
    }

    /**
     * Convert byte array specified position to int value
     *
     * @param src    Byte array
     * @param srcPos start position
     * @param len    Conversion length ,Cannot be greater than 4
     * @return int
     */
    public static int bytesToInt(byte[] src, int srcPos, int len) {
        return bytesToInt(src, srcPos, len, true);
    }

    /**
     * Convert byte array specified position to int value
     *
     * @param src       Byte array
     * @param srcPos    start position
     * @param len       Conversion length ,Cannot be greater than 4
     * @param bigEndian Is it big endian?
     * @return long
     */
    public static int bytesToInt(byte[] src, int srcPos, int len, boolean bigEndian) {
        if (src == null) {
            throw new InvalidParameterException("The src array cannot be null");
        }
        if (srcPos < 0 || len < 0) {
            throw new InvalidParameterException("The starting index or length cannot be less than 0");
        }
        if (len > 4) {
            throw new InvalidParameterException("The length cannot be greater than the maximum length of int 4 bytes");
        }
        int end = srcPos + len;
        if (end > src.length) {
            throw new InvalidParameterException("Length out of bounds");
        }
        int value = 0;
        for (int i = 0; i < len; i++) {
            int offset;
            if (bigEndian) {
                offset = len - 1 - i;
            } else {
                offset = i;
            }
            value |= (src[i + srcPos] & 0xFF) << (offset << 3);
        }
        return value;
    }

    /**
     * Calculate and return the CRC-16 of a buffer
     *
     * @param input 待校验的数组
     * @param len   待校验长度
     * @return 校验值， 两个byte
     */
    public static int calcCrc16(byte[] input, int len) {
        int crc16 = 0x0000;
        int crcH8, crcL8;
        for (int i = 0; i < len; i++) {
            crcH8 = (crc16 >> 8) & 0xFFFF;
            crcL8 = (crc16 << 8) & 0xFFFF;
            crc16 = crcL8 ^ CRC_TABLE[crcH8 ^ (input[i] & 0xFF)];
        }
        return crc16;
    }

    /**
     * Calculate and return the CRC-32 of a buffer
     *
     * @param input 待校验的数组
     * @param len   待校验长度
     * @return 校验值， 四个byte
     */
    public static int calcCrc32(byte[] input, int len) {
        int i, j;
        int index;
        int crc = CRC32_SEED;
        int polyRev = 0;
        int poly = POLY32;
        int[] table = new int[256];
        int c;
        //位逆转
        for (i = 0; i < 32; i++) {
            if ((poly & 0x01) != 0) {
                polyRev |= 1 << (31 - i);
            }
            poly >>>= 1;
        }
        //码表生成
        for (i = 0; i < 256; i++) {
            c = i;
            for (j = 0; j < 8; j++) {
                c = ((c & 1) > 0) ? (polyRev ^ (c >>> 1)) : (c >>> 1);
            }
            table[i] = c;
        }
        //计算CRC
        for (i = 0; i < len; i++) {
            index = (input[i] ^ crc) & 0xFF;
            crc = ((crc >>> 8) ^ table[index]);
        }
        crc ^= 0xFFFFFFFF;
        return crc;
    }

    /**
     * Calculate checksum
     *
     * @param bytes     data
     * @param fromIndex start position
     * @param len       Checking length
     * @return Checksum
     */
    public static byte getCheckSum(byte[] bytes, int fromIndex, int len) {
        byte btSum = 0x00;

        for (int nloop = fromIndex; nloop < fromIndex + len; nloop++) {
            btSum += bytes[nloop];
        }
        return (byte) (((~btSum) + 1) & 0xFF);
    }

    /**
     * Print out the array
     *
     * @param data     Byte array
     * @param interval Interval
     * @param hex      Is it a hexadecimal string
     */
    public static void logcat(byte[] data, int interval, boolean hex) {
        if (data == null) {
            return;
        }
        int dataLen = data.length;
        Log.e("gpenghui", "Length: " + dataLen);
        int index = 0;
        while (index < dataLen) {
            int len;
            if (index + interval < dataLen) {
                len = interval;
            } else {
                len = dataLen - index;
            }
            Log.e("gpenghui", "Data[ " + ArrayUtils.bytesToString(data, index, len, hex, " ") + " ]");
            index += len;
        }
    }

    public static int getu16(byte[] data, int offset) {
        return getu16at(data, offset) & 0xffff;
    }

    public static short gets16(byte[] data, int offset) {
        short val;
        val = (short) (getu16at(data, offset) & 0xffff);
        return val;
    }

    public static int getu16at(byte[] data, int offset) {
        return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
    }

    public static void setu16at(byte[] data, int offset, int val) {
        data[offset] = (byte) ((val >> 8) & 0xff);
        data[offset + 1] = (byte) (val & 0xff);
    }

    public static byte[] to2Arr(int s) {
        byte b1 = (byte) ((s >> 8) & 0x00FF);
        byte b2 = (byte) (s & 0x00FF);
        return new byte[]{b1, b2};
    }

    public static short CRC16_XMODEM(byte[] puchMsg, int offset, int usDataLen) {
        //BYTE unsigned 0x0ff
        //WORD unsigned 0x0ffff
        //DWORD unsigned 0x0ffffffff
        short wCRCin = 0x0000;
        short wCPoly = 0x1021;
        byte wChar = 0;
        int i;
        int msgIndex = offset;

        while ((usDataLen--) > 0) {
            wChar = (byte) (puchMsg[msgIndex++] & 0x0ff);
            wCRCin ^= (wChar << 8);
            for (i = 0; i < 8; i++) {
                if ((wCRCin & 0x8000) != 0) {
                    wCRCin = (short) ((wCRCin << 1) ^ wCPoly);
                } else {
                    wCRCin = (short) (wCRCin << 1);
                }
            }
        }
        return (wCRCin);
    }
}
