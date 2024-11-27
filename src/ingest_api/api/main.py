from fastapi import FastAPI
from confluent_kafka import Producer
from appsettings import Settings
from models.orderitem import OrderItem, OrderItemWithMerchant
from models.productinventory import ProductInventory, ProductInventoryWithMerchant


def delivery_report(err, msg):
    if err is not None:
        print('Delivery failed for User record {}: {}'.format(msg.key(), err))
        return
    print('Record {} successfully produced to {} [{}] at offset {}'.format(
        msg.key(), msg.topic(), msg.partition(), msg.offset()))


settings = Settings()
app = FastAPI()
producer = Producer({'bootstrap.servers': settings.kafka_bootstrap_servers})


@app.get("/")
async def root():
    return "Nothing"

@app.post("/api/{merchantId}/order")
async def write_order_data(merchantId: str, item: OrderItem):
    # TODO: Get the merchant Id based on authentication for real...
    composite_key = f"{merchantId}#{item.orderId}#{item.productId}"
    topic_item = OrderItemWithMerchant(**item.model_dump(), merchantId=merchantId)

    producer.produce(settings.productorder_topic, key=composite_key, value=topic_item.model_dump_json(), on_delivery=delivery_report)
    producer.flush()
    return {"status": "OK"}

@app.post("/api/{merchantId}/inventory")
async def write_inventory_data(merchantId: str, item: ProductInventory):
    # TODO: Get the merchant Id based on authentication for real...
    composite_key = f"{merchantId}#{item.productId}"
    topic_item = ProductInventoryWithMerchant(**item.model_dump(), merchantId=merchantId)

    producer.produce(settings.inventory_topic, key=composite_key, value=topic_item.model_dump_json(), on_delivery=delivery_report)
    producer.flush()
    return {"status": "OK"}
