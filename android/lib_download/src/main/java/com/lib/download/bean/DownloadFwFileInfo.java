package com.lib.download.bean;

import android.text.TextUtils;

import com.lib.download.enu.DownloadChipType;
import com.lib.download.enu.DownloadFwType;
import com.lib.download.util.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

public class DownloadFwFileInfo {
    private static final int NORMAL_PACKET_SIZE = 400;
    private static final int RFID_PACKET_SIZE = 240;
    private static final int XCN_PACKET_SIZE = 1024 * 4;
    private int mSendPacketSize = NORMAL_PACKET_SIZE;

    private String fileVersion;
    private String buildTime;
    private DownloadFwType fwType;
    private DownloadChipType chipType;
    private Integer encryptedMode;
    private byte[] mData;
    private int fileCrc32;
    private int dataCrc32;

    public DownloadFwFileInfo() {
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] data) {
        mData = data;
    }

    public String getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(String fileVersion) {
        this.fileVersion = fileVersion;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public DownloadFwType getFwType() {
        return fwType;
    }

    public void setFwType(DownloadFwType fwType) {
        this.fwType = fwType;
    }

    public DownloadChipType getChipType() {
        return chipType;
    }

    public void setChipType(DownloadChipType chipType) {
        this.chipType = chipType;
    }

    public int getFileCrc32() {
        return fileCrc32;
    }

    public void setFileCrc32(int fileCrc32) {
        this.fileCrc32 = fileCrc32;
    }

    public int getDataCrc32() {
        return dataCrc32;
    }

    public void setDataCrc32(int dataCrc32) {
        this.dataCrc32 = dataCrc32;
    }

    public Integer getEncryptedMode() {
        return encryptedMode;
    }

    public void setEncryptedMode(Integer encryptedMode) {
        this.encryptedMode = encryptedMode;
    }

    public int getSendPacketSize() {
        return mSendPacketSize;
    }

    public void setSendPacketSize(int sendPacketSize) {
        mSendPacketSize = sendPacketSize;
    }

    @Override
    public String toString() {
        return "{fileVersion='" + fileVersion +
                ", buildTime='" + buildTime +
                ", fwType=" + fwType +
                ", chipType=" + chipType +
                ", encryptedMode=" + encryptedMode +
                ", data.length=" + (mData == null ? 0 : mData.length) +
                ", sendPacketSize=" + mSendPacketSize +
                ", fileCrc32=" + String.format("%08X", fileCrc32) +
                ", dataCrc32=" + String.format("%08X", dataCrc32) +
                '}';
    }

    /**
     * Obtain file data according to the incoming file path
     *
     * @param name the system-dependent file name.
     * @return Byte array
     */
    private static byte[] getFileData(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        File file = new File(name);
        if (!file.exists()) {
            return null;
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int available = bis.available();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(available);
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = bis.read(buffer, 0, bufferSize)) > 0) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static byte[] getFileDataOnce(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        File file = new File(name);
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream is = new FileInputStream(file);
            byte[] dest = new byte[is.available()];
            is.read(dest);
            return dest;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Obtain file information based on bin file data
     *
     * @param filePath 文件路径
     * @return see {@link DownloadFwFileInfo}
     */
    public static DownloadFwFileInfo getFwFileInfo(String filePath, boolean chip) {
        byte[] data = getFileData(filePath);
        if (data == null || data.length == 0) {
            return null;
        }
        if (chip) {
            int i = data[0x81] & 0xFF;

            String version = "";
            String buildTime = "";
            DownloadFwType fwType = DownloadFwType.CHIP;
            DownloadChipType chipType = DownloadChipType.CHIP_E710;
            int fileCrc32 = 0;
            int dataCrc32 = ArrayUtils.calcCrc32(data, data.length);
            Integer encryptedMode = null;/*偶数加密的*/

            DownloadFwFileInfo fileInfo = new DownloadFwFileInfo();
            fileInfo.setFileVersion(version);
            fileInfo.setBuildTime(buildTime);
            fileInfo.setFwType(fwType);
            fileInfo.setChipType(chipType);
            fileInfo.setFileCrc32(fileCrc32);
            fileInfo.setDataCrc32(dataCrc32);
            fileInfo.setEncryptedMode(encryptedMode);
            fileInfo.setData(data);
            fileInfo.setSendPacketSize(RFID_PACKET_SIZE);
            return fileInfo;
        }

        int limitMinLen = 0x8F;
        if (data.length < limitMinLen) {
            return null;
        }
        int i = data[0x81] & 0xFF;
        String version = String.format(Locale.getDefault(), "v%d.%d.%d.%d",
                data[0x80] & 0xFF,
                i,
                data[0x82] & 0xFF,
                data[0x83] & 0xFF);
        String buildTime = String.format(Locale.getDefault(), "%02d-%02d-%02d %02d:%02d:%02d",
                data[0x84] & 0xFF,
                data[0x85] & 0xFF,
                data[0x86] & 0xFF,
                data[0x87] & 0xFF,
                data[0x88] & 0xFF,
                data[0x89] & 0xFF);
        DownloadFwType fwType = DownloadFwType.valueOf(data[0x8A]);/*保留后面3字节*/
        DownloadChipType chipType = DownloadChipType.valueOf(data[0x8B]);// 固件类型 0x8b
        if (chipType == DownloadChipType.CHIP_E710) {/*兼容旧版*/
            byte b = data[0x8F];
            if (b != ((byte) 0xA5)) {
                chipType = DownloadChipType.CHIP_R2000;
            }
        }
        int encryptedMode = (i & 1);/*偶数加密的*/

        int validLength = data.length - 4;
        int fileCrc32 = ArrayUtils.bytesToInt(data, validLength, 4, false);
        int dataCrc32 = ArrayUtils.calcCrc32(data, validLength);

        byte[] dest = new byte[validLength];
        System.arraycopy(data, 0, dest, 0, validLength);

        DownloadFwFileInfo fileInfo = new DownloadFwFileInfo();
        fileInfo.setFileVersion(version);
        fileInfo.setBuildTime(buildTime);
        fileInfo.setFwType(fwType);
        fileInfo.setChipType(chipType);
        fileInfo.setFileCrc32(fileCrc32);
        fileInfo.setDataCrc32(dataCrc32);
        fileInfo.setEncryptedMode(encryptedMode);
        fileInfo.setData(dest);
        if (chipType == DownloadChipType.CHIP_PRINTER) {
            fileInfo.setSendPacketSize(XCN_PACKET_SIZE);
        } else {
            fileInfo.setSendPacketSize(NORMAL_PACKET_SIZE);
        }
        return fileInfo;
    }
}
