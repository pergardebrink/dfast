package com.dfast.app;

import org.apache.flink.api.java.functions.KeySelector;

public class OrderItemKeySelector implements KeySelector<OrderItem, CompositeKey> {
    @Override
    public CompositeKey getKey(OrderItem orderItem) {
        return new CompositeKey(orderItem.getMerchantId(), orderItem.getProductId());
    }
}
