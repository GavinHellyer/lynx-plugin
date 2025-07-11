package com.ultimafurniture.lynx;

import com.ultimafurniture.lynx.models.Inventory;
import com.ultimafurniture.lynx.models.OperationTag;

public interface RFIDReaderListener {

    void onScanningStatus(boolean status);

    void onInventoryTag(Inventory inventory);

    void onOperationTag(OperationTag operationTag);

    void onInventoryTagEnd(Inventory.InventoryTagEnd tagEnd);

    void onConnection(boolean status);
}
