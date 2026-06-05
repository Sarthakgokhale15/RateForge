How to use RateForge — Quick, practical guide

1) Build & install the library (one-time)

```bash
mvn clean install -DskipTests
```

2) Run a demo app (example-hello)

- Start Redis (example): `redis-server --port 6379 --save "" --appendonly no --daemonize yes`
- Run the demo app:

```bash
mvn -f example-hello/pom.xml spring-boot:run
```
- Exercise `/hello` or run the demo script:

```bash
./example-hello/demo_tier_test.sh free 15
./example-hello/demo_tier_test.sh pro 15
```

3) Enable Kafka analytics (optional, opt-in per app)

- In the consuming application's `application.yml` set:

```yaml
rate-limiter:
  kafka:
    enabled: true
    bootstrap-servers: broker1:9092,broker2:9092
    topic: rateforge.events
```

- If you need TLS/SASL, register your own `ProducerFactory`/`KafkaTemplate` bean in the application — auto-configuration will back off.

4) Minimal `application.yml` example for a consumer

```yaml
spring:
  redis:
    host: redis-host
    port: 6379

rate-limiter:
  default-algorithm: TOKEN_BUCKET
  policies:
    free-users:
      algorithm: TOKEN_BUCKET
      capacity: 60
      refillTokensPerSecond: 1.0
    pro-users:
      algorithm: TOKEN_BUCKET
      capacity: 600
      refillTokensPerSecond: 20.0
  web:
    enabled: true
    key-header: X-User-Id
    tier-header: X-User-Tier
    default-policy-name: free-users
    tier-policies:
      free: free-users
      pro: pro-users

# Optional analytics
  kafka:
    enabled: false
    bootstrap-servers: localhost:9092
    topic: rateforge.events
```

5) Customization hooks (optional)

- Provide `RateLimitKeyResolver` to control how the key is derived (JWT, account id, IP).
- Provide `RateLimitPolicyResolver` to control policy resolution (path, roles, tenant).
- Provide custom `RateLimiterAlgorithm` beans to implement new algorithms.

6) Operational and security notes

- Supply sensitive settings via env vars / Kubernetes Secrets (Spring Boot relaxed binding supported).
- Provide 2–3 bootstrap brokers for Kafka resiliency.
- If you do not want analytics at all, set `rate-limiter.kafka.enabled=false`.

7) Troubleshooting

- If analytics do not appear: ensure `rate-limiter.kafka.enabled=true` and a `KafkaTemplate` bean exists (or let auto-config create one by setting bootstrap servers).
- If rate limiting behaves unexpectedly: inspect `X-RateLimit-*` headers returned by the app for policy and remaining tokens.

If you want, I can also generate a short `docker-compose.demo.yml` (Redis + Zookeeper + Kafka) for local reproducible demos.
