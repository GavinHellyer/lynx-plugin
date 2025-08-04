package com.lib.download.reader;

import com.lib.download.util.ArrayUtils;

import java.security.InvalidParameterException;

/**
 * Protocol data processing
 * Data (excluding data header, length, address, cmd and checksum)
 * // -------------------------------------------------------------
 * // | header | length | address |  cmd  |   [data]   | checksum |
 * // |   1B   |   2B   |    1B   |  1B   | 0~(512-4)B |    2B    |
 * // -------------------------------------------------------------
 */

public class DataPacketFC extends AbstractDataPacket {
    public DataPacketFC() {
        HEAD = HEAD_FC;
        MIN_MSG_LEN = 7;
    }

    public AbstractDataPacket create(byte address, Object cmd) {
        return create(address, cmd, null);
    }

    /**
     * @param address  Reader address
     * @param cmd      Command code
     * @param coreData Core data
     */
    public AbstractDataPacket create(byte address, Object cmd, byte[] coreData) {
        int coreDataLen = 0;
        if (!ArrayUtils.isEmpty(coreData)) {
            coreDataLen = coreData.length;
        }
        int len = coreDataLen + 4;
        byte[] sendBytes = new byte[MIN_MSG_LEN + coreDataLen];
        sendBytes[0] = HEAD;
        sendBytes[1] = (byte) (len >> 8);
        sendBytes[2] = (byte) len;
        sendBytes[3] = address;
        sendBytes[4] = (byte) cmd;
        if (coreDataLen > 0) {
            System.arraycopy(coreData, 0, sendBytes, 5, coreDataLen);
        }
        int crc16 = ArrayUtils.calcCrc16(sendBytes, sendBytes.length - 2);
        sendBytes[sendBytes.length - 2] = (byte) (crc16 >> 8);
        sendBytes[sendBytes.length - 1] = (byte) crc16;
        this.mMsgBytes = sendBytes;
        return this;
    }

    /**
     * source data
     *
     * @param bytes byte[]
     * @return
     */
    public AbstractDataPacket parseMsg(byte[] bytes) throws InvalidParameterException {
        //Minimum protocol data length
        // -------------------------------------------------------------
        // | header | length | address |  cmd  |   [data]   | checksum |
        // |   1B   |   2B   |    1B   |  1B   | 0~(512-4)B |    2B    |
        // -------------------------------------------------------------

        if (bytes == null || bytes.length < MIN_MSG_LEN) {
            throw new InvalidParameterException("Incorrect byte array length");
        }
        if (bytes[0] != HEAD) {
            throw new InvalidParameterException("Not a FC message.");
        }
        int checkSum = ArrayUtils.getu16(bytes, bytes.length - 2);

        int crc16 = ArrayUtils.calcCrc16(bytes, bytes.length - 2);
        if (crc16 != checkSum) {
            throw new InvalidParameterException("crc16 != checkSum [" + ArrayUtils.bytesToHexString(bytes) + "]");
        }

        this.mMsgBytes = bytes;
        return this;
    }

    /**
     * Get data length, The number of bytes since the packet starts from Len, does not contain Len itself.
     *
     * @return The second byte of the packet
     */
    public int getMsgLen() {
        return ArrayUtils.getu16(mMsgBytes, 1);
    }

    /**
     * Get reader address, Used when the RS-485 interface is connected in series. The general address is from 0 to 254 (0xFE) and 255 (0xFF) is the public address.
     * The reader receives commands for its own address and public address
     *
     * @return The third byte of the packet
     */
    public byte getAddress() {
        return mMsgBytes[3];
    }

    /**
     * Get command code
     *
     * @return The fourth byte of the packet
     */
    public Object getCmd() {
        return mMsgBytes[4];
    }

    @Override
    public int startIndex() {
        return 5;
    }
}
