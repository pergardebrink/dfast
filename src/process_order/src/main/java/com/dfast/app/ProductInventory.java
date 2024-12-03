package com.dfast.app;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductInventory {
    @JsonProperty("merchantId")
    private String merchantId;

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("category")
    private String category;

    @JsonProperty("subCategory")
    private String subCategory;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }
}
