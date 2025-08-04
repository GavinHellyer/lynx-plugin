package com.ultimafurniture.lynx;


import com.payne.reader.bean.receive.OperationTag;
import com.payne.reader.bean.receive.InventoryTag;
import com.payne.reader.bean.receive.InventoryTagEnd;


public interface RFIDReaderListener {

    void onScanningStatus(boolean status);

    void onInventoryTag(InventoryTag inventory);

    void onOperationTag(OperationTag operationTag);

    void onInventoryTagEnd(InventoryTagEnd tagEnd);

    void onConnection(boolean status);

     void onOutputPower(int value);
}
