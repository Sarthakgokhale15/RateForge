Local Kafka for RateForge

Quick start (requires Docker):

1. Start Zookeeper + Kafka:

```bash
docker-compose -f docker-compose.kafka.yml up -d
```

2. Verify Kafka is running:

```bash
# list topics (requires kafka tools or use docker exec)
docker run --network host --rm confluentinc/cp-kafka:7.4.0 kafka-topics --bootstrap-server localhost:9092 --list
```

3. Enable analytics in `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

rate-limiter:
  kafka:
    enabled: true
    bootstrap-servers: localhost:9092
    topic: rateforge.events
```

4. Run RateForge locally (jar or docker) and observe topic messages. Use `kafka-console-consumer` to see events:

```bash
# using docker image with tools
docker run --network host --rm confluentinc/cp-kafka:7.4.0 kafka-console-consumer \
  --bootstrap-server localhost:9092 --topic rateforge.events --from-beginning
```

Notes
- On macOS with Docker Desktop, `KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092` usually works for local testing.
- For production, use proper advertised listeners and security (SASL/SSL), and tune replication factors.
