package com.dfast.app;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public class OrderItem {
    @JsonProperty("merchantId")
    private String merchantId;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("productId")
    private String productId;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("quantity")
    private int quantity;
    @JsonProperty("shippingCost")
    private float shippingCost;
    @JsonProperty("amount")
    private float amount;
    @JsonProperty("channel")
    private Optional<String> channel;
    @JsonProperty("channelGroup")
    private Optional<String> channelGroup;
    @JsonProperty("campaign")
    private Optional<String> campaign;
    @JsonProperty("dateTime")
    private String dateTime;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(float shippingCost) {
        this.shippingCost = shippingCost;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Optional<String> getChannel() {
        return channel;
    }

    public void setChannel(Optional<String> channel) {
        this.channel = channel;
    }

    public Optional<String> getChannelGroup() {
        return channelGroup;
    }

    public void setChannelGroup(Optional<String> channelGroup) {
        this.channelGroup = channelGroup;
    }

    public Optional<String> getCampaign() {
        return campaign;
    }

    public void setCampaign(Optional<String> campaign) {
        this.campaign = campaign;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
