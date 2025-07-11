package com.ultimafurniture.lynx.barcode.base;

/**
 * Created by lei.li on 2016-12-12.
 * The class of return data package;
 */
public class QueryMessageTran {
    private byte[] prefix;
    private byte[] lens;
    private byte type;
    private byte[] data;
    private byte checkSum;
    private byte[] allData;

    public QueryMessageTran() {
    }

    public QueryMessageTran(byte[] rxData) {
        if (rxData == null) {
            throw new NullPointerException("Data source is empty!");
        }
        byte init = checkSum(rxData, 2, rxData.length - 3);
        if (init != rxData[rxData.length - 1]) {
            return;
        }
        prefix = new byte[2];
        lens = new byte[2];
        data = new byte[rxData.length - 6];
        allData = new byte[rxData.length];
        System.arraycopy(rxData, 0, prefix, 0, 2);
        System.arraycopy(rxData, 2, lens, 0, 2);
        type = rxData[4];
        checkSum = rxData[rxData.length - 1];
        System.arraycopy(rxData, 5, data, 0, data.length);
        System.arraycopy(rxData, 0, allData, 0, allData.length);

    }

    /**
     * Calculate checksum
     * 
     * @param btAryBuffer data
     * @param nStartPos   start position
     * @param nLen        Checking length
     * @return Checksum
     */
    public static byte checkSum(byte[] btAryBuffer, int nStartPos, int nLen) {
        byte btSum = (byte) 0xFF;
        for (int nloop = nStartPos; nloop < nStartPos + nLen; nloop++) {
            btSum ^= btAryBuffer[nloop];
        }
        return btSum;
    }

    /**
     * build scanner query instruction.
     * 
     * @return
     */
    public static byte[] buildQueryCMD(byte[] command) {
        if (command == null) {
            throw new NullPointerException("Command is not allowed Null!");
        }
        byte[] cmd = new byte[command.length + 6];
        System.arraycopy(Command.QUERY_CMD_SEND_PREFIX, 0, cmd, 0, 2);
        int len = 1 + command.length;
        byte[] lens = new byte[2];
        lens[0] = (byte) (len / 256);
        lens[1] = (byte) (len % 256);
        System.arraycopy(lens, 0, cmd, 2, 2);
        System.arraycopy(Command.QUERY_CMD_SEND_TYPE, 0, cmd, 4, 1);
        System.arraycopy(command, 0, cmd, 5, command.length);
        byte checksum = checkSum(cmd, 2, cmd.length - 3);
        cmd[cmd.length - 1] = checksum;
        return cmd;
    }

    public byte[] getPrefix() {
        return prefix;
    }

    public void setPrefix(byte[] prefix) {
        this.prefix = prefix;
    }

    public byte[] getLens() {
        return lens;
    }

    public void setLens(byte[] lens) {
        this.lens = lens;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getAllData() {
        return allData;
    }

    public void setAllData(byte[] allData) {
        this.allData = allData;
    }
}
