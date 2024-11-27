from pydantic import BaseModel


class Product(BaseModel):
    merchantId: str
    productId: str
    name: str
    quantity: int
    category: str
    subCategory: str
