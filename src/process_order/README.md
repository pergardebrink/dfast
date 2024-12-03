mvn package
docker-compose exec jobmanager ./bin/flink run -d /opt/flink/jobs/process-order-1.0.jar
