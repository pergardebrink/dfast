from pydantic import BaseModel


class ProductInventory(BaseModel):
    productId: str
    name: str
    quantity: int
    category: str
    subCategory: str


class ProductInventoryWithMerchant(ProductInventory):
    merchantId: str
