package com.lib.download.reader;

import com.lib.download.util.ArrayUtils;

import java.security.InvalidParameterException;

public abstract class AbstractDataPacket {
    public static final byte HEAD_FC = (byte) 0xFC;
    public static final byte HEAD_A0 = (byte) 0xA0;
    public static final byte HEAD_B0 = (byte) 0xB0;
    protected byte HEAD;
    protected int MIN_MSG_LEN;
    /**
     * Raw byte array
     */
    protected byte[] mMsgBytes;

    /**
     * @param address Reader address
     * @param cmd     Command code
     * @return
     */
    abstract public AbstractDataPacket create(byte address, Object cmd);

    /**
     * @param address  Reader address
     * @param cmd      Command code
     * @param coreData Core data
     */
    abstract public AbstractDataPacket create(byte address, Object cmd, byte[] coreData);

    /**
     * source data
     *
     * @param bytes byte[]
     * @return
     */
    abstract public AbstractDataPacket parseMsg(byte[] bytes) throws InvalidParameterException;

    /**
     * Get packet header
     *
     * @return First byte of packet
     */
    public byte getHeader() {
        return mMsgBytes[0];
    }

    /**
     * Get data length, The number of bytes since the packet starts from Len, does not contain Len itself.
     *
     * @return The second byte of the packet
     */
    abstract public int getMsgLen();

    /**
     * Get raw data
     *
     * @return byte[]
     */
    public byte[] getMsgBytes() {
        return mMsgBytes;
    }

    /**
     * Get reader address, Used when the RS-485 interface is connected in series. The general address is from 0 to 254 (0xFE) and 255 (0xFF) is the public address.
     * The reader receives commands for its own address and public address
     *
     * @return The third byte of the packet
     */
    abstract public byte getAddress();

    /**
     * Get command code
     *
     * @return The fourth byte of the packet
     */
    abstract public Object getCmd();

    /**
     * Get the starting index of the core data in the data packet (the fifth byte, the index is 4)
     *
     * @return starting index
     */
    abstract public int startIndex();

    /**
     * Get the length of the core data in the packet (Packet length - (head + len + address + cmd + checksum)), Need to check if the length is 0
     *
     * @return Core data length
     */
    public int getCoreDataLen() {
        return mMsgBytes.length - MIN_MSG_LEN;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "-[" + ArrayUtils.bytesToHexString(mMsgBytes) + ']';
    }
}