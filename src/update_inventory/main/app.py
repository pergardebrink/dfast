from confluent_kafka import Consumer, KafkaError
from appsettings import Settings

settings = Settings()


print("Starting inventory consumer...")
print(f"Kafka: {settings.kafka_bootstrap_servers}")
print(f"Inventory Topic: {settings.inventory_topic}")


consumer_config = {
    'bootstrap.servers': settings.kafka_bootstrap_servers,
    'group.id': 'inventory-consumer-group',
    'auto.offset.reset': 'earliest'
}

consumer = Consumer(consumer_config)
consumer.subscribe([settings.inventory_topic])

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
finally:
    consumer.close()
    print("Consumer closed")
