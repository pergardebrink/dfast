from typing import Optional
from pydantic import BaseModel


class EnrichedProductWithMerchant(BaseModel):
    merchantId: str
    orderId: str
    productId: str
    name: str
    quantity: int
    category: str
    subCategory: str
    currency: str
    quantity: int
    shippingCost: float
    amount: float
    channel: Optional[str]
    channelGroup: Optional[str]
    campaign: Optional[str]
    dateTime: str
