from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    kafka_bootstrap_servers: str
    dynamodb_url: str
    dynamodb_table: str
    inventory_topic: str
