package com.lib.download.reader;

import com.lib.download.util.ArrayUtils;

import java.security.InvalidParameterException;

/**
 * Protocol data processing
 * Data (excluding data header, length, address, cmd and checksum)
 * // -------------------------------------------------------------
 * // | header | length | address |  cmd  |   [data]   | checksum |
 * // |   1B   |   1B   |    1B   |  1B   | 0~(512-4)B |    1B    |
 * // -------------------------------------------------------------
 */

public class DataPacketA0 extends AbstractDataPacket {
    public DataPacketA0() {
        HEAD = HEAD_A0;
        MIN_MSG_LEN = 5;
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
        byte[] sendBytes = new byte[MIN_MSG_LEN + coreDataLen];
        sendBytes[0] = HEAD;
        sendBytes[1] = (byte) (coreDataLen + 3);
        sendBytes[2] = address;
        sendBytes[3] = (byte) cmd;
        if (coreDataLen > 0) {
            System.arraycopy(coreData, 0, sendBytes, 4, coreDataLen);
        }

        byte checkSum = ArrayUtils.getCheckSum(sendBytes, 0, sendBytes.length - 1);
        sendBytes[sendBytes.length - 1] = checkSum;
        this.mMsgBytes = sendBytes;
        return this;
    }

    public AbstractDataPacket parseMsg(byte[] rawData) {
        // [Hdr Len Addr Cmd Data Check]
        // [1   1   1    1   N    1    ]
        if (rawData == null || rawData.length < MIN_MSG_LEN) {
            throw new InvalidParameterException("Incorrect byte array length");
        }
        if (rawData[0] != HEAD) {
            throw new InvalidParameterException("Not a A0 message.");
        }
        byte checkSum = rawData[rawData.length - 1];

        byte calCheck = ArrayUtils.getCheckSum(rawData, 0, rawData.length - 1);
        if (checkSum != calCheck) {
            throw new InvalidParameterException("calCheckSum != checkSum for " + ArrayUtils.bytesToHexString(rawData));
        }
        this.mMsgBytes = rawData;
        return this;
    }

    public int getMsgLen() {
        return mMsgBytes[1] & 0xFF;
    }

    public byte getAddress() {
        return mMsgBytes[2];
    }

    public Object getCmd() {
        return mMsgBytes[3];
    }

    @Override
    public int startIndex() {
        return 4;
    }
}