import json
import boto3
from confluent_kafka import Consumer, Producer, KafkaError
from appsettings import Settings
from models.orderitem import OrderItemWithMerchant
from models.productinventory import EnrichedProductWithMerchant, ProductInventoryWithMerchant

settings = Settings()

print("Starting product order enricher...")
print(f"Kafka: {settings.kafka_bootstrap_servers}")
print(f"ProductOrder Topic: {settings.productorder_topic}")
print(f"ProductOrderEnriched Topic: {settings.productorder_enriched_topic}")
print(f"DynamoDB: {settings.dynamodb_url}")

consumer_config = {
    'bootstrap.servers': settings.kafka_bootstrap_servers,
    'group.id': 'enrich-consumer-group',
    'auto.offset.reset': 'earliest'
}

consumer = Consumer(consumer_config)
consumer.subscribe([settings.productorder_topic])

producer = Producer({'bootstrap.servers': settings.kafka_bootstrap_servers})
dynamodb = boto3.resource('dynamodb', endpoint_url=settings.dynamodb_url)
table = dynamodb.Table(settings.dynamodb_table)


def query_dynamodb(merchant_id, product_id):
    response = table.get_item(
        Key={
            'merchantId': merchant_id,
            'productId': product_id
        }
    )
    return response.get('Item')


def process_message(message):
    product_data = json.loads(message.value().decode('utf-8'))
    order_item = OrderItemWithMerchant(**product_data)
    dynamo_data: ProductInventoryWithMerchant = query_dynamodb(order_item.merchantId, order_item.productId)
    print(dynamo_data)
    if dynamo_data:
        enriched_product = EnrichedProductWithMerchant(
            merchantId=order_item.merchantId,
            productId=order_item.productId,
            orderId=order_item.orderId,
            name=dynamo_data["name"],
            category=dynamo_data["category"],
            subCategory=dynamo_data["subCategory"],
            currency=order_item.currency,
            quantity=order_item.quantity,
            shippingCost=order_item.shippingCost,
            amount=order_item.amount,
            channel=order_item.channel,
            channelGroup=order_item.channelGroup,
            campaign=order_item.campaign,
            dateTime=order_item.dateTime,
        )
        composite_key = f"{order_item.merchantId}#{order_item.orderId}#{order_item.productId}"
        producer.produce(settings.productorder_enriched_topic, key=composite_key, value=enriched_product.model_dump_json())
        producer.flush()
        print(f"Enriched and sent data for orderId {order_item.orderId}, productId {order_item.productId}")


try:
    while True:
        msg = consumer.poll(1.0)
        if msg is None:
            continue
        if msg.error():
            if msg.error().code() == KafkaError._PARTITION_EOF:
                continue
            else:
                print(msg.error())
                break
        print(f"Message received: {msg.value().decode('utf-8')}")
        process_message(msg)
finally:
    consumer.close()
    print("Consumer closed")
