package com.ultimafurniture.lynx;

public interface Reader {
    enum ReaderType {
        RFID, BARCODE;
    }

    void connect(ReaderType type);

    boolean isConnected();
}
