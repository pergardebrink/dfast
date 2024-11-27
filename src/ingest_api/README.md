# This is a simple ingestion API that accepts input data from merchants


## Available endpoints: 

### Order endpoint
POST http://localhost:4444/api/{merchantId}/order

Body: 
{
    "orderId,": "",
    "productId,": "",
    "currency,": "",
    "quantity,": "",
    "shippingCost,": "",
    "amount,": "",
    "channel,": "",
    "channelGroup,": "",
    "campaign,": "",
    "dateTime": ""
}

### Inventory endpoint
POST http://localhost:4444/api/{merchantId}/inventory

Body: 
{
    "productId": "",
    "name": "",
    "quantity": "",
    "category": "",
    "subCategory": ""
}
