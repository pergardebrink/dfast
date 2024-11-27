import json
import boto3
from confluent_kafka import Consumer, KafkaError
from appsettings import Settings
from models.product import Product

settings = Settings()


print("Starting inventory consumer...")
print(f"Kafka: {settings.kafka_bootstrap_servers}")
print(f"Inventory Topic: {settings.inventory_topic}")
print(f"DynamoDB: {settings.dynamodb_url}")

dynamodb = boto3.resource('dynamodb', endpoint_url=settings.dynamodb_url)
table = dynamodb.Table(settings.dynamodb_table)

consumer_config = {
    'bootstrap.servers': settings.kafka_bootstrap_servers,
    'group.id': 'inventory-consumer-group',
    'auto.offset.reset': 'earliest'
}

consumer = Consumer(consumer_config)
consumer.subscribe([settings.inventory_topic])


def process_message(message):
    product_data = json.loads(message.value().decode('utf-8'))
    product = Product(**product_data)
    table.put_item(Item=product.model_dump())
    # TODO: Handle out of order updates to only replace if newer?


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
