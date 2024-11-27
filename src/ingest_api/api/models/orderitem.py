from pydantic import BaseModel
from typing import Optional


class OrderItem(BaseModel):
    orderId: str
    productId: str
    currency: str
    quantity: int
    shippingCost: float
    amount: float
    channel: Optional[str]
    channelGroup: Optional[str]
    campaign: Optional[str]
    dateTime: str


class OrderItemWithMerchant(OrderItem):
    merchantId: str
