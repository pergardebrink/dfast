from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    kafka_bootstrap_servers: str
    productorder_enriched_topic: str
    minio_url: str
    minio_access_key: str
    minio_secret_key: str
