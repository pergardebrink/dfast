package com.dfast.app;

import org.apache.flink.api.java.functions.KeySelector;

public class ProductInventoryItemKeySelector implements KeySelector<ProductInventory, CompositeKey> {
    @Override
    public CompositeKey getKey(ProductInventory inventoryItem) {
        return new CompositeKey(inventoryItem.getMerchantId(), inventoryItem.getProductId());
    }
}
