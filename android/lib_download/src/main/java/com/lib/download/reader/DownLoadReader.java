package com.lib.download.reader;

import android.os.SystemClock;

import com.lib.download.bean.DownloadCheckCrcResult;
import com.lib.download.bean.DownloadFwFileInfo;
import com.lib.download.bean.DownloadVersionResult;
import com.lib.download.enu.DownloadChipType;
import com.lib.download.enu.DownloadFwCheckType;
import com.lib.download.enu.DownloadFwType;
import com.lib.download.enu.DownloadResultCode;
import com.lib.download.enu.DownloadStage;
import com.lib.download.util.ArrayUtils;
import com.payne.reader.base.Consumer;
import com.payne.reader.communication.ConnectHandle;
import com.payne.reader.util.LLLog;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DownLoadReader implements Consumer<byte[]> {
    private static final int MAX_RETRY_COUNT = 6;

    private final Object mLockObj = new Object();

    private Consumer<byte[]> mSendRawDataCallback;
    private Consumer<AbstractDataPacket> mReceiveRawDataCallback;
    private volatile Consumer<byte[]> mMRpsl;

    private boolean mEnablePassthrough;
    private OnPassthroughStatusListener mOnPassthroughStatusListener;

    private ExecutorService sEs;
    private byte mAddress = (byte) 0xFF;
    private byte mLastHeader;
    private Object mLastCmd;
    private int mTimeoutMillis = 6000;
    private int mRetryCount;
    private byte[] mCancelBs = new byte[]{-80, 0, 6, -1, 16, 1, 24, -105, 57};
    private byte[] mResumeBs = new byte[]{-80, 0, 6, -1, 16, 1, 21, 70, -108};
    private byte[] mPauseBs = new byte[]{-80, 0, 6, -1, 16, 1, 20, 86, -75};
    public final byte[] PRINTER_UPDATE_END = new byte[]{-4, 0, 5, 1, 5, 2, -50, -99};

    private DownloadFwFileInfo mFileInfo;
    /**
     * Used to store unprocessed byte data
     */
    private byte[] mFCUnprocessedBytes = null;
    private byte[] mA0UnprocessedBytes = null;
    private byte[] mB0UnprocessedBytes = null;

    private volatile AbstractDataPacket mReceivePacket;
    private ConnectHandle mConnectHandle;

    private static class Inner {
        private final static DownLoadReader INSTANCE = new DownLoadReader();
    }

    private DownLoadReader() {
        sEs = Executors.newFixedThreadPool(1);
    }

    public static DownLoadReader getInstance() {
        return Inner.INSTANCE;
    }

    public void bindConnectHandle(ConnectHandle connectHandle) {
        mConnectHandle = connectHandle;
    }

    public void setSendRawDataListener(Consumer<byte[]> c) {
        mSendRawDataCallback = c;
    }

    public void setReceiveRawDataListener(Consumer<AbstractDataPacket> c) {
        mReceiveRawDataCallback = c;
    }

    public void setReceivePrinterStatusListener(Consumer<byte[]> mRpsl) {
        mMRpsl = mRpsl;
    }

    public void setOnPassthroughStatusListener(OnPassthroughStatusListener l) {
        mOnPassthroughStatusListener = l;
    }

    public boolean isConnect() {
        return mConnectHandle != null && mConnectHandle.isConnected();
    }

    public boolean isEnablePassthrough() {
        return mEnablePassthrough;
    }

    @Override
    public void accept(byte[] bytes) {
        if (Arrays.equals(bytes, mCancelBs)) {
            mEnablePassthrough = false;
            if (mOnPassthroughStatusListener != null) {
                mOnPassthroughStatusListener.onPassthrough(false);
            }
            return;
        }
        if (LLLog.isDebug()) {
            String hexStr = ArrayUtils.bytesToHexString(bytes);
            LLLog.i(Arrays.toString(bytes) + "---\n" + hexStr);
        }

        if (mMRpsl != null) {
            try {
                mMRpsl.accept(bytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Arrays.equals(bytes, PRINTER_UPDATE_END)) {
                clearBuff();
            }
            return;
        }

        AbstractDataPacket packet = null;
        if (mLastHeader == AbstractDataPacket.HEAD_FC) {
            packet = analyzeDataFC(bytes);
        } else if (mLastHeader == AbstractDataPacket.HEAD_A0) {
            packet = analyzeDataA0(bytes);
        } else if (mLastHeader == AbstractDataPacket.HEAD_B0) {
            packet = analyzeDataB0(bytes);
        }

        mReceivePacket = packet;

        if (mReceivePacket == null) {
            if (!mEnablePassthrough) {
                LLLog.e("ReceivePacket parsed err!!!");
            }
            return;
        }
        if (mReceiveRawDataCallback != null) {
            try {
                mReceiveRawDataCallback.accept(mReceivePacket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object receivePacketCmd = mReceivePacket.getCmd();
        if (receivePacketCmd instanceof Short) {
            Short packetCmd = (Short) receivePacketCmd;
            if (packetCmd.equals(mLastCmd)) {
                synchronized (mLockObj) {
                    mLockObj.notifyAll();
                }
            } else {
                int arg1 = ((short) mLastCmd & 0xFFFF);
                LLLog.i(String.format("LastCmd=%04x,receivePacketCmd=%04x", arg1, packetCmd));
            }
        }
        if (receivePacketCmd instanceof Byte) {
            Byte packetCmd = (Byte) receivePacketCmd;
            if (packetCmd.equals(mLastCmd)) {
                synchronized (mLockObj) {
                    mLockObj.notifyAll();
                }
            } else {
                int arg1 = ((byte) mLastCmd & 0xFF);
                LLLog.i(String.format("LastCmd=%02x,receivePacketCmd=%02x", arg1, packetCmd));
            }
        }
    }

    /**
     * Set the timeout receiving time after sending
     *
     * @param timeoutMillis overtime time
     */
    public void setTimeout(int timeoutMillis) {
        if (timeoutMillis < 0) {
            timeoutMillis = 0;
        }
        this.mTimeoutMillis = timeoutMillis;
    }

    public boolean init() {
        try {
            AbstractDataPacket packet = new DataPacketA0().create((byte) 0x01, (byte) 0xD0);
            sendRawData(packet);/*init*/
            Thread.sleep(200);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 软件复位读写器
     *
     * @return 参考 {@link DownloadResultCode}
     */
    public DownloadResultCode resetFC() {
        LLLog.i("resetFC......");
        AbstractDataPacket send = new DataPacketFC().create(mAddress, DownLoadCmd.CMD_BL_SOFTWARE_RESET);
        AbstractDataPacket receive = sendAndReceive(send, 3000);
        if (receive == null) {
            return DownloadResultCode.FAIL;
        }
        byte[] data = receive.getMsgBytes();
        int len = receive.getCoreDataLen();
        int fromIndex = send.startIndex();
        if (len != 1) {
            return DownloadResultCode.FAIL;
        }
        return DownloadResultCode.valueOf(data[fromIndex]);
    }

    public void resetToBootLoadA0() {
        if (mFileInfo.getFwType() != DownloadFwType.CHIP) {
            byte[] flag = {(byte) 0xA0, 0x04, (byte) 0x01, (byte) 0xAE, (byte) 0xFF, (byte) 0xAE};
            AbstractDataPacket send0 = new DataPacketA0().parseMsg(flag);/*ok*/
            sendAndReceive(send0, 0);
            SystemClock.sleep(100);

            byte[] resetData = {(byte) 0xA0, 0x03, (byte) 0x01, (byte) 0x70, (byte) 0xEC};
            send0 = new DataPacketA0().parseMsg(resetData);/*ok*/
            sendAndReceive(send0, 0);
            SystemClock.sleep(100);
        }
    }

    public void switchPrinterBootLoader() throws Exception {
        sendRawData(new DataPacketB0().create((byte) 0x01, (short) 0x0406));/*打印机重启*/
    }

    public void enablePassthrough() throws Exception {
        mEnablePassthrough = true;
        if (mOnPassthroughStatusListener != null) {
            mOnPassthroughStatusListener.onPassthrough(true);
        }
        AbstractDataPacket packet = new DataPacketB0().create((byte) 0xFF, (short) 0x03F0, new byte[]{0x02});
        sendRawData(packet);/*透传*/
    }
    //<editor-fold desc="VersionInfo">

    /**
     * 读取loader版本和其他信息
     *
     * @return 信息，参考{@link DownloadVersionResult}
     */
    public DownloadVersionResult getVersion(DownloadFwFileInfo df) {
        mFileInfo = df;
        mRetryCount = 0;

        switch (mFileInfo.getFwType()) {
            case FIRMWARE:
                if (!mEnablePassthrough && mFileInfo.getChipType() == DownloadChipType.CHIP_PRINTER) {
                    return getPrinterInfoB0((short) 0x0401);
                } else {
//                    if (!mEnablePassthrough) {
//                        resetFC();
//                    }
                    resetToBootLoadA0();
                    return getVersionFC();
                }
            case CHIP:
                if (mEnablePassthrough || mFileInfo.getChipType() != DownloadChipType.CHIP_PRINTER) {
                    return getVersionA0();
                }
            default:
                return new DownloadVersionResult(DownloadResultCode.FAIL);
        }
    }

    public DownloadVersionResult getRFIDVersion(DownloadFwFileInfo df) {
        mFileInfo = df;
        if (!mEnablePassthrough && mFileInfo.getChipType() == DownloadChipType.CHIP_PRINTER) {
            return getPrinterInfoB0((short) 0x0409);
        } else {
            return getRFIDVersionA0();
        }
    }

    public DownloadVersionResult getVersionA0() {
        LLLog.i("获取版本A0");
        byte cmd = DownLoadCmd.CMD_E710_GET_VERSION;
        AbstractDataPacket send = new DataPacketA0().create(mAddress, cmd);
        AbstractDataPacket receive = sendAndReceive(send);
        if (receive == null) {
            return new DownloadVersionResult(DownloadResultCode.FAIL);
        }
        byte[] data = receive.getMsgBytes();

        int fromIndex = receive.startIndex();
        int coreDataLen = receive.getCoreDataLen();

        DownloadChipType chipType = DownloadChipType.valueOf(data[fromIndex]);
        byte[] verData = new byte[coreDataLen - 1];
        System.arraycopy(data, fromIndex + 1, verData, 0, verData.length);

        String str = new String(verData, StandardCharsets.US_ASCII);
        return new DownloadVersionResult(chipType, str, "none", "0", null);
    }

    public DownloadVersionResult getRFIDVersionA0() {
        LLLog.i("获取RFID版本A0");
        byte[] bytes = {(byte) 0xA0, 0x03, 0x01, 0x72, (byte) 0xEA};
        AbstractDataPacket send = new DataPacketA0().parseMsg(bytes);
        AbstractDataPacket receive = sendAndReceive(send);
        if (receive == null) {
            return new DownloadVersionResult(DownloadResultCode.FAIL);
        }
        byte[] data = receive.getMsgBytes();

        if (data == null || data.length == 0) {
            return new DownloadVersionResult(DownloadResultCode.FAIL);
        }
        int dataLen = receive.getCoreDataLen();
        if (dataLen == 2) {
            DownloadChipType chipType = null;
            int fromIndex = receive.startIndex();
            byte majorByte = data[fromIndex];
            int hv = majorByte >> 5;
            int type = hv & 7;
            switch (type) {
                case 0:
                default:
                    chipType = DownloadChipType.CHIP_R2000;
                case 1:
                    break;
                case 2:
                    chipType = DownloadChipType.CHIP_E710;
                    break;
                case 3:
                    chipType = DownloadChipType.CHIP_TM600;
                    break;
                case 4:
                    chipType = DownloadChipType.CHIP_FDW;
                    break;
            }

            int major = majorByte & 31;
            int minor = data[fromIndex + 1] & 255;
            String strVersion = major + "." + minor;

            return new DownloadVersionResult(chipType, strVersion, "none", "0", null);
        }

        return new DownloadVersionResult(DownloadResultCode.FAIL);
    }

    public DownloadVersionResult getVersionFC() {
        LLLog.i("getVersionFC");
        byte cmd = DownLoadCmd.CMD_BL_GET_VERSION;
        AbstractDataPacket send = new DataPacketFC().create(mAddress, cmd);
        AbstractDataPacket receive = sendAndReceive(send);
        if (receive == null) {
            return new DownloadVersionResult(DownloadResultCode.FAIL);
        }
        byte[] data = receive.getMsgBytes();
        int len = receive.getCoreDataLen();
        int fromIndex = send.startIndex();
        if (len != 6) {
//        if (len != 12) {
            if (len == 1) {
                return new DownloadVersionResult(DownloadResultCode.valueOf(data[fromIndex]));
            }
            return new DownloadVersionResult(DownloadResultCode.FAIL);
        }
        DownloadChipType chipType = DownloadChipType.valueOf(data[fromIndex]);
        byte byteHwVer = data[fromIndex + 1];
        String hwVersion = String.format(Locale.getDefault(), "V%d.%d", (byteHwVer >> 4) & 0x0F, byteHwVer & 0x0F);
        String bootVersion = String.format(Locale.getDefault(), "V%d.%d.%d.%d",
                data[fromIndex + 2] & 0xFF,
                data[fromIndex + 3] & 0xFF,
                data[fromIndex + 4] & 0xFF,
                data[fromIndex + 5] & 0xFF);
        String buildTime = "0";
//        String buildTime = String.format(Locale.getDefault(),
//                "%02d-%02d-%02d %02d:%02d:%02d",
//                data[fromIndex + 6], data[fromIndex + 7],
//                data[fromIndex + 8], data[fromIndex + 9],
//                data[fromIndex + 10], data[fromIndex + 11]);
        int i = data[fromIndex + 3] & 0xFF;
        int encrypted = (i & 1);/*偶数加密的*/
        return new DownloadVersionResult(chipType, hwVersion, bootVersion, buildTime, encrypted);
    }

    public DownloadVersionResult getPrinterInfoB0(short cmd) {
        LLLog.i("获取打印机信息B0");
        AbstractDataPacket dataPacket = new DataPacketB0().create((byte) 0xff, cmd);
        AbstractDataPacket receive = sendAndReceive(dataPacket);

        if (receive == null) {
            return new DownloadVersionResult(DownloadResultCode.FAIL);
        }
        byte[] data = receive.getMsgBytes();
        int coreDataLen = receive.getCoreDataLen();

        byte[] verData = new byte[coreDataLen];
        System.arraycopy(data, receive.startIndex(), verData, 0, verData.length);
        String str = new String(verData, StandardCharsets.US_ASCII);
        return new DownloadVersionResult(DownloadChipType.CHIP_PRINTER, str, "", "", null);
    }
    //</editor-fold>

    /**
     * 开始下载固件到模块
     *
     * @param size 数据总字节数
     * @return 参考 {@link DownloadResultCode}
     */
    public DownloadResultCode startDownload(int size) {
        DownloadFwType fwType = mFileInfo.getFwType();
        boolean isChip = fwType == DownloadFwType.CHIP;
        byte cmd = isChip ? DownLoadCmd.CMD_E710_FW_DOWNLOAD : DownLoadCmd.CMD_BL_FLASH_DOWNLOAD;

        byte[] btArray = new byte[6];
        byte stage = DownloadStage.START_DOWNLOAD.getValue();
        btArray[0] = stage;
        btArray[1] = fwType.getValue();
        AbstractDataPacket send;
        if (isChip) {
            ArrayUtils.intAddToBytes(btArray, 2, size, 4);
            send = new DataPacketA0().create(mAddress, cmd, btArray);
        } else {
            ArrayUtils.intAddToBytes(btArray, 2, size, 4);
            send = new DataPacketFC().create(mAddress, cmd, btArray);
        }
        AbstractDataPacket receive = sendAndReceive(send);
        if (receive == null) {
            if ((++mRetryCount) < MAX_RETRY_COUNT) {
                LLLog.i("retry.startDownload------" + mRetryCount);
                return startDownload(size);
            }
            return DownloadResultCode.FAIL;
        }
        byte[] data = receive.getMsgBytes();
        int len = receive.getCoreDataLen();
        int fromIndex = send.startIndex();
        if (len != 1) {
            return DownloadResultCode.FAIL;
        }
        return DownloadResultCode.valueOf(data[fromIndex]);
    }

    public DownloadResultCode downloading(int frameId, int dataStartAdd, byte[] data) {
        boolean isChip = mFileInfo.getFwType() == DownloadFwType.CHIP;
        byte cmd = isChip ? DownLoadCmd.CMD_E710_FW_DOWNLOAD : DownLoadCmd.CMD_BL_FLASH_DOWNLOAD;

        byte stage = DownloadStage.DOWNLOADING.getValue();
        byte[] btArray = new byte[9 + data.length];
        btArray[0] = stage;
        ArrayUtils.intAddToBytes(btArray, 1, frameId, 2);
        ArrayUtils.intAddToBytes(btArray, 3, dataStartAdd, 4);
        ArrayUtils.intAddToBytes(btArray, 7, data.length, 2);
        System.arraycopy(data, 0, btArray, 9, data.length);
        AbstractDataPacket send;
        if (isChip) {
            send = new DataPacketA0().create(mAddress, cmd, btArray);
        } else {
            send = new DataPacketFC().create(mAddress, cmd, btArray);
        }

        AbstractDataPacket receive = sendAndReceive(send);
        if (receive == null) {
            if ((++mRetryCount) < MAX_RETRY_COUNT) {
                LLLog.i("retry.downloading------" + mRetryCount);
                return downloading(frameId, dataStartAdd, data);
            }
            return DownloadResultCode.FAIL;
        }
        data = receive.getMsgBytes();
        int len = receive.getCoreDataLen();
        int fromIndex = send.startIndex();
        if (len != 3) {
            if (len == 1) {
                return DownloadResultCode.valueOf(data[fromIndex]);
            }
            return DownloadResultCode.FAIL;
        }
        byte receiveStage = data[fromIndex];
        int receiveFrameId = ArrayUtils.bytesToInt(data, fromIndex + 1, 2);
        if (receiveStage != stage || receiveFrameId != frameId) {
            return DownloadResultCode.FAIL;
        }
        mRetryCount = 0;
        return DownloadResultCode.SUCCESS;
    }

    public DownloadResultCode endDownload(int crc32) {
        boolean isChip = mFileInfo.getFwType() == DownloadFwType.CHIP;
        byte cmd = isChip ? DownLoadCmd.CMD_E710_FW_DOWNLOAD : DownLoadCmd.CMD_BL_FLASH_DOWNLOAD;

        byte stage = DownloadStage.END_DOWNLOAD.getValue();
        byte[] btArray = new byte[5];
        btArray[0] = stage;
        ArrayUtils.intAddToBytes(btArray, 1, crc32, 4);
        AbstractDataPacket send;
        if (isChip) {
            send = new DataPacketA0().create(mAddress, cmd, btArray);
        } else {
            send = new DataPacketFC().create(mAddress, cmd, btArray);
        }

        AbstractDataPacket receive = sendAndReceive(send, mTimeoutMillis + 4000);
        if (receive == null) {
            if ((++mRetryCount) < MAX_RETRY_COUNT) {
                LLLog.i("retry.endDownload------" + mRetryCount);
                return endDownload(crc32);
            }
            return DownloadResultCode.FAIL;
        }
        byte[] data = receive.getMsgBytes();
        int len = receive.getCoreDataLen();
        int fromIndex = send.startIndex();
        if (len != 1) {
            return DownloadResultCode.FAIL;
        }
        return DownloadResultCode.valueOf(data[fromIndex]);
    }

    /**
     * flash固件crc32校验
     *
     * @param type see {@link DownloadFwCheckType}
     * @return see {@link DownloadCheckCrcResult}
     */
    public DownloadCheckCrcResult checkCrc(DownloadFwCheckType type) {
        byte cmd = DownLoadCmd.CMD_BL_CHECK_CRC;/*后需添加 e710*/
        byte[] btArray = new byte[1];
        btArray[0] = type.getValue();
        AbstractDataPacket send;
        if (mFileInfo.getFwType() == DownloadFwType.CHIP) {
            send = new DataPacketA0().create(mAddress, cmd, btArray);
        } else {
            send = new DataPacketFC().create(mAddress, cmd, btArray);
        }
        AbstractDataPacket receive = sendAndReceive(send);
        if (receive == null) {
            return new DownloadCheckCrcResult(DownloadResultCode.FAIL);
        }
        byte[] data = receive.getMsgBytes();
        int len = receive.getCoreDataLen();
        int fromIndex = send.startIndex();
        if (len != 8) {
            if (len == 1) {
                return new DownloadCheckCrcResult(DownloadResultCode.valueOf(data[fromIndex]));
            }
            return new DownloadCheckCrcResult(DownloadResultCode.FAIL);
        }
        int crcCheck = ArrayUtils.bytesToInt(data, fromIndex, 4);
        int crcStored = ArrayUtils.bytesToInt(data, fromIndex + 4, 4);
        return new DownloadCheckCrcResult(crcCheck, crcStored);
    }

    public void updateLoader() {
        byte cmd = DownLoadCmd.CMD_BL_LOADER_UPDATE;
        // DataPacket send = new DataPacket(mAddress, cmd);
        //todo
    }


    /**
     * Send and receive data
     *
     * @param sendPacket Data packet to be sent, see {@link AbstractDataPacket}
     * @return Data received, see {@link AbstractDataPacket}
     */
    private AbstractDataPacket sendAndReceive(AbstractDataPacket sendPacket) {
        return sendAndReceive(sendPacket, mTimeoutMillis);
    }

    private AbstractDataPacket sendAndReceive(AbstractDataPacket sendPacket, long timeout) {
        try {
            sendRawData(sendPacket);/*sendAndReceive*/
        } catch (Exception ignored) {
            return null;
        }

        if (timeout > 0) {
            Future<AbstractDataPacket> submit = sEs.submit(new Callable<AbstractDataPacket>() {
                @Override
                public AbstractDataPacket call() throws Exception {
                    synchronized (mLockObj) {
                        mReceivePacket = null;
                        // String cmdName = DownLoadCmd.getNameForCmd(cmd);
                        // LLLog.i(TAG, "start wait -->" + cmdName);
                        mLockObj.wait();
                        //                    LLLog.i(TAG, "be waked up-->" + mReceivePacket);
                        return mReceivePacket;
                    }
                }
            });
            try {
                AbstractDataPacket dataPacket = submit.get(timeout, TimeUnit.MILLISECONDS);
                return dataPacket;
            } catch (Exception e) {
                synchronized (mLockObj) {
                    mLockObj.notifyAll();
                    LLLog.w("接收已超时." + timeout);
                }
            }
        }
        return null;
    }

    public void sendRawData(AbstractDataPacket packet) throws Exception {
        mLastHeader = packet.getHeader();
        mLastCmd = packet.getCmd();
        byte[] msgBytes = packet.getMsgBytes();
        if (mSendRawDataCallback != null) {
            try {
                mSendRawDataCallback.accept(msgBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mConnectHandle == null) {
            LLLog.w("Must call bindConnectHandle at first!");
        } else {
//        LLLog.i("实际发送：" + ArrayUtils.bytesToHexString(msgBytes));
            mConnectHandle.onSend(msgBytes);
        }
    }

    /**
     * Process the received data and perform subcontracting according to the protocol
     * <p>
     * -------------------------------------------------------------
     * | header | length | address |  cmd  |   [data]   | checksum |
     * |   1B   |   2B   |    1B   |  1B   | 0~(512-4)B |    2B    |
     * -------------------------------------------------------------
     */
    private AbstractDataPacket analyzeDataFC(byte[] bytes) {
//        LLLog.i(TAG, "analyzeDataFC--------------------------");
//        LLLog.i(TAG, "analyzeDataFC--------------------------" + ArrayUtils.bytesToHexString(bytes));
        byte[] mergeBytes = bytes;
        if (mFCUnprocessedBytes != null) {
            mergeBytes = ArrayUtils.mergeBytes(mFCUnprocessedBytes, bytes);
//            LLLog.i("合并数据：", ArrayUtils.bytesToHexString(mergeBytes));
        }
        int receiveLen = mergeBytes.length;
        // The minimum length of a packet of data
        int minOnePacketDataLen = 7;
        int index = 0;
        while (index < receiveLen) {
            if (AbstractDataPacket.HEAD_FC != mergeBytes[index]) {
                index++;
                continue;
            }
            // Data length is not enough
            if (index + minOnePacketDataLen > receiveLen) {
                mFCUnprocessedBytes = saveUnprocessedData(mergeBytes, index);
                return null;
            }
            int dataLen = ((mergeBytes[index + 1] << 8) | (mergeBytes[index + 2] & 0xFF)) & 0xFFFF;
            int onePacketDataLen = dataLen + 3;
            if (index + onePacketDataLen > receiveLen) {
                mFCUnprocessedBytes = saveUnprocessedData(mergeBytes, index);
                return null;
            }
            byte[] extractData = new byte[onePacketDataLen];
            System.arraycopy(mergeBytes, index, extractData, 0, onePacketDataLen);
            AbstractDataPacket receivePacket = null;
            try {
                receivePacket = new DataPacketFC().parseMsg(extractData);
            } catch (InvalidParameterException e) {
                e.printStackTrace();
                index++;
                continue;
            }
            this.mAddress = receivePacket.getAddress();
            index += onePacketDataLen;

            if (index < receiveLen) {//处理完整的一包数据之后留下来的数据
                mFCUnprocessedBytes = saveUnprocessedData(mergeBytes, index);
//                LLLog.i("遗留数据[" + mFCUnprocessedBytes.length + "]" + ArrayUtils.bytesToHexString(mFCUnprocessedBytes));
            } else {
//                LLLog.i("无遗留数据");
                clearBuff();
            }
            return receivePacket;
        }
        clearBuff();
        return null;
    }

    /**
     * Process the received data and perform subcontracting according to the protocol
     * <p>
     * -------------------------------------------------------------
     * | header | length | address |  cmd  |   [data]   | checksum |
     * |   1B   |   1B   |    1B   |  1B   | 0~(512-4)B |    1B    |
     * -------------------------------------------------------------
     */
    private AbstractDataPacket analyzeDataA0(byte[] bytes) {
//        LLLog.i(TAG, "analyzeDataA0--------------------------");
//        LLLog.i(TAG, "analyzeDataA0--------------------------" + ArrayUtils.bytesToHexString(bytes, 0, bytes.length, " "));
//        if (mUnprocessedBytess == null) {/*模拟存在其他数据*/
//            mUnprocessedBytess = new byte[]{(byte) 0xA0, (byte) 0x01};
//        }
        byte[] mergeBytes = bytes;
        if (mA0UnprocessedBytes != null) {
            mergeBytes = ArrayUtils.mergeBytes(mA0UnprocessedBytes, bytes);
//            LLLog.i("合并数据：", ArrayUtils.bytesToHexString(mergeBytes));
        }
        int receiveLen = mergeBytes.length;
        // The minimum length of a packet of data
        int minOnePacketDataLen = 5;
        int index = 0;
        while (index < receiveLen) {
            if (AbstractDataPacket.HEAD_A0 != mergeBytes[index]) {
                index++;
                continue;
            }
            // Data length is not enough
            if (index + minOnePacketDataLen > receiveLen) {
                mA0UnprocessedBytes = saveUnprocessedData(mergeBytes, index);
//                LLLog.i(TAG, "analyzeData-------------出局：1");
                return null;
            }
            int dataLen = mergeBytes[index + 1] & 0xFF;
            int onePacketDataLen = dataLen + 2;
            if (index + onePacketDataLen > receiveLen) {
                mA0UnprocessedBytes = saveUnprocessedData(mergeBytes, index);
//                LLLog.i(TAG, "analyzeData-------------出局：2");
                return null;
            }
            byte[] extractData = new byte[onePacketDataLen];
            System.arraycopy(mergeBytes, index, extractData, 0, onePacketDataLen);
            AbstractDataPacket receivePacket = null;
            try {
                receivePacket = new DataPacketA0().parseMsg(extractData);
            } catch (Exception e) {
                e.printStackTrace();
                index++;
                continue;
            }
            this.mAddress = receivePacket.getAddress();
            index += onePacketDataLen;

            if (index < receiveLen) {// 处理完整的一包数据之后留下来的数据
                mA0UnprocessedBytes = saveUnprocessedData(mergeBytes, index);
//                LLLog.i("遗留数据[" + mA0UnprocessedBytes.length + "]" + ArrayUtils.bytesToHexString(mA0UnprocessedBytes));
            } else {
//                LLLog.i("无遗留数据");
                clearBuff();
            }
            return receivePacket;
        }
//        LLLog.i(TAG, "?-------------出局：4");
        clearBuff();
        return null;
    }

    /**
     * Process the received data and perform subcontracting according to the protocol
     * <p>
     * -------------------------------------------------------------
     * | header | length | address |  cmd  |   [data]   | checksum |
     * |   1B   |   2B   |    1B   |  2B   | 0~(512-4)B |    2B    |
     * -------------------------------------------------------------
     */
    private AbstractDataPacket analyzeDataB0(byte[] bytes) {
//        LLLog.i(TAG, "analyzeDataB0--------------------------");
//        LLLog.i(TAG, "analyzeDataB0-->" + ArrayUtils.bytesToHexString(bytes, 0, bytes.length));
//        if (mUnprocessedBytess == null) {/*模拟存在其他数据*/
//            mUnprocessedBytess = new byte[]{(byte) 0xA0, (byte) 0x01};
//        }
        byte[] mergeBytes = bytes;
        if (mB0UnprocessedBytes != null) {
            mergeBytes = ArrayUtils.mergeBytes(mB0UnprocessedBytes, bytes);
//            LLLog.i("合并数据：", ArrayUtils.bytesToHexString(mergeBytes));
        }
        int receiveLen = mergeBytes.length;
        // The minimum length of a packet of data
        int minOnePacketDataLen = 8;
        int index = 0;
        while (index < receiveLen) {
            if (AbstractDataPacket.HEAD_B0 != mergeBytes[index]) {
                index++;
                continue;
            }
            // Data length is not enough
            if (index + minOnePacketDataLen > receiveLen) {
                mB0UnprocessedBytes = saveUnprocessedData(mergeBytes, index);
//                LLLog.i(TAG, "analyzeData-------------出局：1");
                return null;
            }
            int dataLen = ArrayUtils.getu16(mergeBytes, index + 1);
            int onePacketDataLen = dataLen + 3;
            if (index + onePacketDataLen > receiveLen) {
//                LLLog.i(TAG, "analyzeData-------------出局：2");
                mB0UnprocessedBytes = saveUnprocessedData(mergeBytes, index);
                return null;
            }
            byte[] extractData = new byte[onePacketDataLen];
            System.arraycopy(mergeBytes, index, extractData, 0, onePacketDataLen);
            AbstractDataPacket receivePacket = null;
            try {
                receivePacket = new DataPacketB0().parseMsg(extractData);
            } catch (Exception e) {
                e.printStackTrace();
                index++;
                continue;
            }
            this.mAddress = receivePacket.getAddress();
            index += onePacketDataLen;

            if (index < receiveLen) {// 处理完整的一包数据之后留下来的数据
                mB0UnprocessedBytes = saveUnprocessedData(mergeBytes, index);
//                LLLog.i("遗留数据[" + mB0UnprocessedBytes.length + "]" + ArrayUtils.bytesToHexString(mB0UnprocessedBytes));
            } else {
//                LLLog.i("无遗留数据");
                clearBuff();
            }
            return receivePacket;
        }
//        LLLog.i(TAG, "?-------------出局：4");
        clearBuff();
        return null;
    }

    private void clearBuff() {
        mFCUnprocessedBytes = null;
        mA0UnprocessedBytes = null;
        mB0UnprocessedBytes = null;
    }

    /**
     * Save unprocessed data for next processing
     *
     * @param mergeBytes source data
     * @param index      Unprocessed data index
     */
    private byte[] saveUnprocessedData(byte[] mergeBytes, int index) {
        byte[] dest;
        if (index > 0) {
            // The data has been extracted. Use the new array to save the unprocessed data for next use.
            dest = new byte[mergeBytes.length - index];
            System.arraycopy(mergeBytes, index, dest, 0, dest.length);
        } else {
            dest = mergeBytes;
        }
        return dest;
    }

    public void disconnect() {
        if (sEs != null) {
            sEs.shutdown();
        }
        if (mConnectHandle != null) {
            mConnectHandle.onDisconnect();
        }
    }

    public interface OnPassthroughStatusListener {
        void onPassthrough(boolean enablePassthrough);
    }

//    public void release() {
//        disconnect();
//        sEs = null;
//        mSendRawDataCallback = null;
//        mReceiveRawDataCallback = null;
//    }
}