/**
 * filename:ReaderMethod.java
 * File Number:2016-10-12_001
 * Creater:lei li
 * Date:2016-10-12
 * Modified By:lei li
 * Description:Initial Version
 * Version:1.0.0
 */

package com.ultimafurniture.lynx.barcode.base;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import com.ultimafurniture.lynx.barcode.tools.CalculateSpeed;
import com.ultimafurniture.lynx.utils.AppLogger;
import com.ultimafurniture.lynx.utils.StringTool;

public abstract class ReaderBase {
    private WaitThread mWaitThread = null;
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private AppLogger logger;

    private static final String TAG = "ReaderBase";

    /**
     * Connection Lost
     */
    public abstract void onLostConnect();

    /**
     * Rewritable function，This function will be called after parse a packet data.
     *
     * @param messageReceiving return data
     */
    public abstract void analyData(QueryMessageTran messageReceiving);

    // Record unprocessed Receive Data, the main consideration to accept data
    // segment.
    private byte[] m_btAryBuffer = new byte[4096];
    // Record the effective length of unprocessed Receive Data effective length.
    private int m_nLength = 0;

    /**
     * With reference constructor. Construct a Reader with input and output streams.
     *
     * @param in  Input Stream
     * @param out Output Stream
     */
    public ReaderBase(InputStream in, OutputStream out) {
        this.mInStream = in;
        this.mOutStream = out;
        logger = AppLogger.getInstance();

        StartWait();
    }

    public boolean IsAlive() {
        return mWaitThread != null && mWaitThread.isAlive();
    }

    public void StartWait() {
        mWaitThread = new WaitThread();
        mWaitThread.start();
    }

    /**
     * Loop receiving data thread.
     *
     * @author Jie
     */
    private class WaitThread extends Thread {
        private boolean mShouldRunning = true;

        public WaitThread() {
            mShouldRunning = true;
        }

        @Override
        public void run() {

            // add by lei.li 2016/11/17
            byte[] dataArray = new byte[1024];
            // add by lei.li 2016/11/17
            byte[] btAryBuffer = new byte[4096];
            int toall = 0;
            StringBuilder stringBuilder = new StringBuilder();
            while (mShouldRunning) {
                try {
                    int nLenRead = mInStream.read(btAryBuffer);

                    if (nLenRead > 0) {
                        byte[] btAryReceiveData = new byte[nLenRead];
                        logger.i(TAG, "run: " + Arrays.toString(btAryReceiveData));
                        System.arraycopy(btAryBuffer, 0, btAryReceiveData, 0,
                                nLenRead);
                        long wastTime = System.currentTimeMillis();
                        runNew2DCodeCallBack(btAryReceiveData);
                    }
                } catch (IOException e) {
                    logger.i(TAG, "IOEXception: " + e.getLocalizedMessage());
                    onLostConnect();
                    return;
                } catch (Exception e) {
                    logger.i(TAG, "Exception: " + e.getLocalizedMessage());

                    onLostConnect();
                    return;
                }

            }
        }

        public void signOut() {
            mShouldRunning = false;
            interrupt();
        }
    }

    ;

    /**
     * Exit receive thread
     */
    public final void signOut() {
        mWaitThread.signOut();
        try {
            mInStream.close();
            mOutStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Handle the data received from scanner.
     *
     * @param btAryReceiveData the received data.
     */
    private void runNew2DCodeCallBack(byte[] btAryReceiveData) {
        try {

            int nCount = btAryReceiveData.length;

            byte[] btAryBuffer = new byte[nCount + m_nLength];

            System.arraycopy(m_btAryBuffer, 0, btAryBuffer, 0, m_nLength);

            System.arraycopy(btAryReceiveData, 0, btAryBuffer, m_nLength,
                    btAryReceiveData.length);

            int nIndex = 0; // When there is the data A0, record the end point of data.
            int start = 0;
            int end = 0;
            for (int nLoop = 0; nLoop < btAryBuffer.length; nLoop++) {

                // if (btAryBuffer[nLoop] == HEAD.HEAD)
                if (btAryBuffer[nLoop] == (byte) 0x02/* STX (start of text) */) {

                    start = nLoop + 1;
                    // The return value Of scanner start with 0x02 0x00
                    if (btAryBuffer.length > start && btAryBuffer[start] == (byte) 0x00) {

                        if (btAryBuffer.length >= nIndex + 4) {

                            if (Arrays.equals(Arrays.copyOfRange(btAryBuffer, nIndex, nIndex + 2),
                                    Command.QUERY_CMD_RETURN_PREFIX)) {

                                int len = (btAryBuffer[nIndex + 2] & 0xFF) * 256 + btAryBuffer[nIndex + 3];
                                // Log.d(TAG, "len : len" + btAryBuffer[nIndex + 2] +"len:" + btAryBuffer[nIndex
                                // + 3] + "len"+ len);
                                if (btAryBuffer.length >= len + 5) {

                                    QueryMessageTran msg = new QueryMessageTran(
                                            Arrays.copyOfRange(btAryBuffer, nIndex, nIndex + len + 5));
                                    commandQueryValue(msg.getData());
                                    nIndex = len + 5;
                                }
                            }
                        }
                    } else {

                        for (int i = nLoop; i < btAryBuffer.length; i++) {

                            if (btAryBuffer[i] == (byte) 0x03/* ETX (end of text) */) {

                                end = i;

                                receive2DCodeData(new String(StringTool.subBytes(logger, btAryBuffer, start, end),
                                        StringTool.charsetName(logger,
                                                StringTool.subBytes(logger, btAryBuffer, start, end))));
                                // calculate the scan speed;
                                CalculateSpeed.mTotalTime += System.currentTimeMillis() - CalculateSpeed.mStartTime;
                                nIndex = i + 1;
                            }
                        }
                    }
                } else if (btAryBuffer[nLoop] == Command.CMD_SET_SUCCESS
                        || btAryBuffer[nLoop] == Command.CMD_SET_FAILD) {

                    commandSetStatus(btAryBuffer[nLoop]);
                    nIndex = nLoop + 1;
                } else {
                }
            }

            if (nIndex <= btAryBuffer.length) {

                m_nLength = btAryBuffer.length - nIndex;
                Arrays.fill(m_btAryBuffer, 0, 4096, (byte) 0);
                System.arraycopy(btAryBuffer, nIndex, m_btAryBuffer, 0,
                        btAryBuffer.length - nIndex);
                // Log.e("nIndex + m_nLength", m_nLength + ":::" + nIndex);
            }
        } catch (Exception e) {
            logger.i(TAG, "Exception-runNew2DCodeCallBack: " + e.getLocalizedMessage());
        }
    }

    /**
     * Scanner attribute query value.
     *
     * @param value
     */
    public void commandQueryValue(byte[] value) {

    }

    /**
     * Callback when set set command to scanner.
     *
     * @param status 0x06 success;0x15 failed.
     */
    public void commandSetStatus(byte status) {

    }

    /**
     * recive2DCodeData
     */
    public void receive2DCodeData(String string) {
        logger.i(TAG, "receive2DCodeData: " + string);
    }

    /**
     * Rewritable function，This function will be called after sending data.
     *
     * @param btArySendData Transmitted Data
     */
    public void sendData(byte[] btArySendData) {
    }

    /**
     * Send data，Use synchronized() to prevent concurrent operation.
     *
     * @param btArySenderData To send data
     * @return Succeeded :0, Failed:-1
     */
    private int sendMessage(byte[] btArySenderData) {

        try {
            synchronized (mOutStream) { // Prevent Concurrent
                mOutStream.write(btArySenderData);
            }
        } catch (IOException e) {
            onLostConnect();
            return -1;
        } catch (Exception e) {
            return -1;
        }

        /**
         * send cmd log information.
         */
        Log.i(TAG, "Send value:" + StringTool.byteArrayToString(btArySenderData, 0, btArySenderData.length));
        sendData(btArySenderData);
        return 0;
    }

    /**
     * Reset 2D scanner.
     *
     * @return
     */
    public final int reset() {
        return sendBuffer(Command.DEFAULTE_FACTORY.getBytes());
    }

    /**
     * Set 2D scanner attribution.Single parameter.
     *
     * @return
     */
    public final int setScannerAttr(byte[] commandCode, byte[] parameter) {
        byte[] buildCMD = buildSetCommand(commandCode, parameter);
        byte[] openSetCMD = Command.OPEN_CMD_SET_CODE.getBytes();
        byte[] closeSetCMD = Command.CLOSE_CMD_SET_CODE.getBytes();
        byte[] setCMD = new byte[openSetCMD.length + buildCMD.length
                + closeSetCMD.length];
        System.arraycopy(openSetCMD, 0, setCMD, 0, openSetCMD.length);
        System.arraycopy(buildCMD, 0, setCMD, openSetCMD.length, buildCMD.length);
        System.arraycopy(closeSetCMD, 0, setCMD, openSetCMD.length + buildCMD.length, closeSetCMD.length);
        return sendBuffer(setCMD);
    }

    /**
     * Set more than one commands;
     *
     * @param map key is command code,and value is set value(value allow null);
     * @return
     */
    public final int setScannerAttr(Map<byte[], byte[]> map) {
        byte[] openSetCMD = Command.OPEN_CMD_SET_CODE.getBytes();
        byte[] closeSetCMD = Command.CLOSE_CMD_SET_CODE.getBytes();
        int len = 0;
        len = openSetCMD.length + closeSetCMD.length;
        for (byte[] key : map.keySet()) {
            len += buildSetCommand(key, map.get(key)).length;
            if (len > 100) {
                throw new RuntimeException("The command bytes over 100 bytes!");
            }
        }

        byte[] setCMD = new byte[len];
        System.arraycopy(openSetCMD, 0, setCMD, 0, openSetCMD.length);
        int index = openSetCMD.length;
        byte[] buildCMD;
        for (byte[] key : map.keySet()) {
            buildCMD = buildSetCommand(key, map.get(key));
            System.arraycopy(buildCMD, 0, setCMD, index, buildCMD.length);
            index += buildCMD.length;
        }
        System.arraycopy(closeSetCMD, 0, setCMD, index, closeSetCMD.length);
        return sendBuffer(setCMD);
    }

    /**
     * Query scanner attribute information.
     *
     * @param parameter the needed query command.
     * @return
     */
    public int queryScannerAttr(byte[] parameter) {
        return sendBuffer(QueryMessageTran.buildQueryCMD(parameter));
    }

    /**
     * Send the command
     *
     * @param completeCMD the command
     * @return Succeeded :0, Failed:-1
     */
    public final int sendBuffer(byte[] completeCMD) {

        int nResult = sendMessage(completeCMD);

        return nResult;
    }

    /**
     * 构建设置指令;
     *
     * @param commandCode 指令码
     * @param parameter   指令参数
     * @return
     */
    private byte[] buildSetCommand(byte[] commandCode, byte[] parameter) {
        if (commandCode == null) {
            throw new NullPointerException("Command code can not allow null!");
        }
        byte[] cmd;
        if (parameter == null) {
            byte[] tmp = Command.SET_CMD_PREFIX.getBytes();
            byte[] tmp2 = Command.CMD_TRAIL.getBytes();
            cmd = new byte[tmp.length + commandCode.length + tmp2.length];
            System.arraycopy(tmp, 0, cmd, 0, tmp.length);
            System.arraycopy(commandCode, 0, cmd, tmp.length, commandCode.length);
            System.arraycopy(tmp2, 0, cmd, tmp.length + commandCode.length, tmp2.length);
        } else {
            byte[] tmp = Command.SET_CMD_PREFIX.getBytes();
            byte[] tmp1 = Command.SET_VALUE_SYMBOL.getBytes();
            byte[] tmp2 = Command.CMD_TRAIL.getBytes();
            cmd = new byte[tmp.length + commandCode.length + tmp1.length + parameter.length + tmp2.length];
            System.arraycopy(tmp, 0, cmd, 0, tmp.length);
            System.arraycopy(commandCode, 0, cmd, tmp.length, commandCode.length);
            System.arraycopy(tmp1, 0, cmd, tmp.length + commandCode.length, tmp1.length);
            System.arraycopy(parameter, 0, cmd, tmp.length + commandCode.length + tmp1.length, parameter.length);
            System.arraycopy(tmp2, 0, cmd, tmp.length + commandCode.length + tmp1.length + parameter.length,
                    tmp2.length);
        }
        return cmd;
    }
}
