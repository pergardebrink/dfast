from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    kafka_bootstrap_servers: str
    inventory_topic: str
    productorder_topic: str
