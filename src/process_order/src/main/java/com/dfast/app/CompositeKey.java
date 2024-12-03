package com.dfast.app;

import java.util.Objects;

public class CompositeKey {
    private String merchantId;
    private String productId;

    public CompositeKey() {}

    public CompositeKey(String merchantId, String productId) {
        this.merchantId = merchantId;
        this.productId = productId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeKey that = (CompositeKey) o;
        return Objects.equals(merchantId, that.merchantId) &&
               Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(merchantId, productId);
    }
}
