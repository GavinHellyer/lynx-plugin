package com.lib.download.reader;

import com.lib.download.util.ArrayUtils;

import java.security.InvalidParameterException;

/**
 * Protocol data processing
 * Data (excluding data header, length, address, cmd and checksum)
 * // -------------------------------------------------------------
 * // | header | length | address |  cmd  |   [data]   | checksum |
 * // |   1B   |   2B   |    1B   |  2B   | 0~(512-4)B |    2B    |
 * // -------------------------------------------------------------
 */

public class DataPacketB0 extends AbstractDataPacket {
    public DataPacketB0() {
        HEAD = HEAD_B0;
        MIN_MSG_LEN = 8;
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
        int index = 0;
        sendBytes[index] = HEAD;
        index++;
        ArrayUtils.setu16at(sendBytes, index, (byte) (coreDataLen + 5));
        index += 2;
        sendBytes[index] = address;
        index++;
        ArrayUtils.setu16at(sendBytes, index, (short) cmd);
        index += 2;
        if (coreDataLen > 0) {
            System.arraycopy(coreData, 0, sendBytes, index, coreDataLen);
            index += coreDataLen;
        }
        short check = ArrayUtils.CRC16_XMODEM(sendBytes, 0, sendBytes.length - 2);
        ArrayUtils.setu16at(sendBytes, index, check);
        this.mMsgBytes = sendBytes;
        return this;
    }

    public AbstractDataPacket parseMsg(byte[] rawData) {
        // [Hdr Len Addr Cmd Data Check]
        // [1   2   1    2   N    2    ]
        if (rawData == null || rawData.length < MIN_MSG_LEN) {
            throw new InvalidParameterException("Incorrect byte array length");
        }
        if (rawData[0] != HEAD) {
            throw new InvalidParameterException("Not a B0 message.");
        }
        short checkSum = ArrayUtils.gets16(rawData, rawData.length - 2);

        short crc16Xmodem = ArrayUtils.CRC16_XMODEM(rawData, 0, rawData.length - 2);
        if (checkSum != crc16Xmodem) {
            String checkSumStr = ArrayUtils.bytesToHexString(ArrayUtils.to2Arr(checkSum));
            String hexStr = ArrayUtils.bytesToHexString(ArrayUtils.to2Arr(crc16Xmodem));
            throw new InvalidParameterException(String.format("CheckSum[%s] not equals calCheck[%s].", checkSumStr, hexStr));

        }
        this.mMsgBytes = rawData;
        return this;
    }

    public int getMsgLen() {
        return ArrayUtils.getu16(mMsgBytes, 1);
    }

    public byte getAddress() {
        return mMsgBytes[3];
    }

    public Object getCmd() {
        return ArrayUtils.gets16(mMsgBytes, 4);
    }

    @Override
    public int startIndex() {
        return 6;
    }
}