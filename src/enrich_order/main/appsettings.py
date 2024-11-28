from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    kafka_bootstrap_servers: str
    productorder_topic: str
    productorder_enriched_topic: str
    dynamodb_url: str
    dynamodb_table: str
