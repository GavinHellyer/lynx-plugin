package com.lib.download;

import android.content.Context;

import com.lib.download.bean.DownloadFwFileInfo;
import com.lib.download.bean.DownloadVersionResult;
import com.lib.download.enu.DownloadChipType;
import com.lib.download.enu.DownloadResultCode;
import com.lib.download.reader.AbstractDataPacket;
import com.lib.download.reader.DownLoadReader;
import com.payne.reader.base.Consumer;
import com.payne.reader.util.LLLog;

import java.util.Arrays;

public class DownloadHelper {
    private volatile boolean mStop;
    private Context mContext;
    private OnDownloadListener mL;
    private DownLoadReader mReader;

    public DownloadHelper(Context context, OnDownloadListener l) {
        mContext = context;
        mReader = DownLoadReader.getInstance();
        mL = l;
    }

    public DownloadHelper setSendRawDataListener(Consumer<byte[]> c) {
        mReader.setSendRawDataListener(c);
        return this;
    }

    public DownloadHelper setReceiveRawDataListener(Consumer<AbstractDataPacket> c) {
        mReader.setReceiveRawDataListener(c);
        return this;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setOnPassthroughStatusListener(DownLoadReader.OnPassthroughStatusListener l) {
        mReader.setOnPassthroughStatusListener(l);
    }

    public boolean isEnablePassthrough() {
        return mReader.isEnablePassthrough();
    }

    public void enablePassthrough() throws Exception {
        mReader.enablePassthrough();
    }

    public DownloadVersionResult getVersion(DownloadFwFileInfo fwFileInfo) {
        return mReader.getVersion(fwFileInfo);
    }

    public DownloadVersionResult getRFIDVersion(DownloadFwFileInfo fwFileInfo) {
        return mReader.getRFIDVersion(fwFileInfo);
    }

    public void release() {
        mReader.disconnect();
        mContext = null;
    }

    /**
     * 更新固件
     *
     * @param fwFileInfo     待下载的文件封装类
     * @param ignoreFileCrc  忽略校验
     * @param ignoreChipType 忽略芯片
     * @param ignoreVersion  忽略版本
     */
    public void update(DownloadFwFileInfo fwFileInfo, boolean ignoreFileCrc, boolean ignoreChipType, boolean ignoreVersion) {
        if ("main".equalsIgnoreCase(Thread.currentThread().getName())) {
            dispatchMsg("Only run not main thread！");
            return;
        }

        if (!ignoreFileCrc) {
            int fileCrc32 = fwFileInfo.getFileCrc32();
            int dataCrc32 = fwFileInfo.getDataCrc32();
            if (fileCrc32 != dataCrc32) {
                String str = String.format("%s\nFileCrc: %08X\nDataCrc: %08X", mContext.getString(R.string.file_check_error), fileCrc32, dataCrc32);
                dispatchMsg(str);
                return;
            }
        }
        DownloadChipType fileChipType = fwFileInfo.getChipType();

        DownloadVersionResult versionResult = mReader.getVersion(fwFileInfo);
        if (!ignoreVersion) {
            if (versionResult.getResultCode() != DownloadResultCode.SUCCESS) {
                String message = mContext.getString(R.string.obtain_version_info_error);
                dispatchMsg(message);
                return;
            }

            if (!ignoreChipType) {
                DownloadChipType softChipType = versionResult.getChipType();
                if (!fileChipType.equals(softChipType)) {
                    String message = mContext.getString(R.string.the_chip_type_not_match) +
                            mContext.getString(R.string.file_is) + fileChipType +
                            mContext.getString(R.string.firmware_is) + softChipType;
                    dispatchMsg(message);
                    return;
                }
                if (softChipType == DownloadChipType.CHIP_E710) {
                    Integer fileEncryptedMode = fwFileInfo.getEncryptedMode();
                    if (fileEncryptedMode != null && !fileEncryptedMode.equals(versionResult.getEncryptedMode())) {
                        String message = mContext.getString(R.string.encrypted_info);
                        dispatchMsg(message);
                        return;
                    }
                }
            }
        }

        if (fileChipType == DownloadChipType.CHIP_PRINTER) {
            try {
                mReader.switchPrinterBootLoader();
            } catch (Exception e) {
                dispatchMsg(mContext.getString(R.string.start_update_error) + e.getMessage());
                return;
            }
        }
        byte[] data = fwFileInfo.getData();
        mL.onMax(data.length);

        DownloadResultCode resultCode = mReader.startDownload(data.length);
//        LLLog.i("resultCode:" + resultCode);
//        DownloadResultCode resultCode = DownloadResultCode.SUCCESS;
        if (resultCode != DownloadResultCode.SUCCESS) {
            dispatchMsg(mContext.getString(R.string.start_update_error) + resultCode);
            return;
        }
        mStop = false;
        int index = 0;
        int frameId = 0;
        int sendPacketSize = fwFileInfo.getSendPacketSize();
        while (index < data.length) {
            if (mStop) {
                LLLog.w("Stopped by user.");
                return;
            }
            int sendLen;
            if (index + sendPacketSize < data.length) {
                sendLen = sendPacketSize;
            } else {
                sendLen = data.length - index;
            }

            byte[] sendData = new byte[sendLen];
            System.arraycopy(data, index, sendData, 0, sendLen);
//            if (sendLen != sendPacketSize) {
//                LLLog.i("data.length：" + data.length + ",sendLen=" + sendLen);
//            } else {
//                LLLog.i("总长度/剩余：" + data.length + "/" + index);
//            }
            resultCode = mReader.downloading(frameId, index, sendData);
//            resultCode = DownloadResultCode.SUCCESS;
            if (resultCode != DownloadResultCode.SUCCESS) {
                dispatchMsg(mContext.getString(R.string.update_error) + resultCode);
                // LLLog.i("", "index=" + index + ", frameId=" + frameId);
                return;
            }
            mL.onProgress(index, "");

            frameId++;
            index += sendLen;
        }
        LLLog.i("Download finish and valid.");
        resultCode = mReader.endDownload(fwFileInfo.getDataCrc32());
//        resultCode = DownloadResultCode.SUCCESS;
        if (resultCode != DownloadResultCode.SUCCESS) {
            dispatchMsg(mContext.getString(R.string.update_ending_error) + resultCode);
            return;
        }
        if (fwFileInfo.getChipType() == DownloadChipType.CHIP_PRINTER) {
            Consumer<byte[]> listener = new Consumer<byte[]>() {
                int count;

                @Override
                public void accept(byte[] bytes) throws Exception {
                    if (Arrays.equals(mReader.PRINTER_UPDATE_END, bytes)) {
                        mReader.setReceivePrinterStatusListener(null);
                        mL.onProgress(data.length, "OK");
                    } else {
                        mL.onProgress(-1, "[" + (++count) + "]");
                    }
                }
            };
            mReader.setReceivePrinterStatusListener(listener);
        } else {
            mL.onProgress(data.length, "");
        }

//            resultCode = Reader.getInstance()
//                    .reset();
//            if (resultCode != ResultCode.SUCCESS) {
//                Log.e(TAG, "Reset error: " + resultCode);
//                dispatchError(emitter, "复位模块失败！");
//                return;
//            }
    }

    private void dispatchMsg(String message) {
        if (mL != null) {
            mL.onError(message);
        }
    }

    public void stopDownload() {
        mStop = true;
    }

    public interface OnDownloadListener {
        void onMax(int max);

        void onProgress(int progress, String msg);

        void onError(String msg);
    }
}