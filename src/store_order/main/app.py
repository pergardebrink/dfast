from io import BytesIO
import signal
import sys
from datetime import datetime
import json
from confluent_kafka import Consumer, KafkaError
from minio import Minio
from appsettings import Settings
from models.productinventory import EnrichedProductWithMerchant

settings = Settings()

print("Starting product order store...")
print(f"Kafka: {settings.kafka_bootstrap_servers}")
print(f"ProductOrderEnriched Topic: {settings.productorder_enriched_topic}")

consumer_config = {
    'bootstrap.servers': settings.kafka_bootstrap_servers,
    'group.id': 'store-consumer-group',
    'auto.offset.reset': 'earliest'
}

client = Minio(
    settings.minio_url,
    access_key=settings.minio_access_key,
    secret_key=settings.minio_secret_key,
    secure=False
)

bucket_name = "data-lake"

consumer = Consumer(consumer_config)
consumer.subscribe([settings.productorder_enriched_topic])

data_buffer = {}


def flush_merchantdata(merchant_id: str):
    print(f"Flushing buffer for merchant {merchant_id}")
    timestamp = datetime.now()
    folder = timestamp.strftime("year=%Y/month=%m/day=%d/hour=%H_00")
    full_datetime = timestamp.strftime("%Y%m%d_%H%M%S")
    file_name = f"merchant={merchant_id}/{folder}/{merchant_id}_{full_datetime}.json"

    data_dicts = [item.dict() for item in data_buffer[merchant_id]]
    json_data = json.dumps(data_dicts).encode('utf-8')
    data_stream = BytesIO(json_data)
    client.put_object(
        bucket_name,
        file_name,
        data_stream,
        len(json_data),
        content_type='application/json'
    )
    data_buffer[merchant_id] = []


def flush_buffer():
    print("Flushing buffer")
    for merchant_id, data in data_buffer.items():
        if data:
            flush_merchantdata(merchant_id)

    data_buffer.clear()


def process_message(message):
    product_data = json.loads(message.value().decode('utf-8'))
    data = EnrichedProductWithMerchant(**product_data)
    merchant_id = data.merchantId

    if merchant_id not in data_buffer:
        data_buffer[merchant_id] = []

    data_buffer[merchant_id].append(data)

    if len(data_buffer[merchant_id]) >= 10:
        flush_merchantdata(merchant_id)


def signal_handler(sig, frame):
    print('Shutdown signal received. Flushing buffer...')
    flush_buffer()
    sys.exit(0)


signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)


try:
    while True:
        msg = consumer.poll(10)
        if msg is None:
            # If no new messages has arrived in the last X seconds, we flush to minio
            flush_buffer()
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

print('Shutdown Flushing buffer...')
flush_buffer()
