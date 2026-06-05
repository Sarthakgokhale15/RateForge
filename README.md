# RateForge

Redis-backed, distributed, pluggable rate-limiter library for Spring Boot.

## What You Get

- Algorithms: Fixed Window, Sliding Window Log, Sliding Window Counter, Token Bucket, Leaky Bucket.
- Lua-backed atomic updates for token and leaky buckets.
- Token Bucket as the default algorithm.
- Starter-style auto configuration for minimal integration effort.
- Tier-aware request limiting (for example, free vs pro users) via request headers and path policies.

## Use As Dependency In Any Spring Boot App

Add dependency (install/publish this artifact in your Maven repo first):

```xml
<dependency>
  <groupId>io.github.sarthakgokhale</groupId>
  <artifactId>rateforge</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Minimal Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379

rate-limiter:
  default-algorithm: TOKEN_BUCKET
  policies:
    free-users:
      algorithm: TOKEN_BUCKET
      capacity: 60
      refill-tokens-per-second: 1.0
      window-seconds: 60
      requested-tokens: 1
    pro-users:
      algorithm: TOKEN_BUCKET
      capacity: 600
      refill-tokens-per-second: 20.0
      window-seconds: 60
      requested-tokens: 1
  web:
    enabled: true
    key-header: X-User-Id
    tier-header: X-User-Tier
    default-policy-name: free-users
    tier-policies:
      free: free-users
      pro: pro-users
```

With this config, your app gets request-level rate limiting automatically.

## E-commerce / Content Site Example

Client sends headers:
- `X-User-Id: u-101`
- `X-User-Tier: free` or `X-User-Tier: pro`

The library resolves the policy:
- `free` -> `free-users`
- `pro` -> `pro-users`

Rate limit decisions are enforced in a servlet filter for all incoming requests except excluded paths.

## Optional Path-Based Policies

```yaml
rate-limiter:
  web:
    path-policies:
      /api/pro/**: pro-users
      /api/free/**: free-users
```

Path policies are checked first, then tier mapping, then default policy.

## Customization Hooks

You can override defaults by providing your own beans:
- `RateLimitKeyResolver`
- `RateLimitPolicyResolver`

If custom beans are present, auto-configuration backs off.

## Local Build

```bash
mvn clean package -DskipTests
```

This produces:
- Standard dependency jar: `target/rateforge-1.0.0.jar`
- Optional executable jar: `target/rateforge-1.0.0-exec.jar`

## Quick Showcase (recommended)

1. Build and install the library to your local Maven repository:

```bash
mvn clean install -DskipTests
```

2. Start Redis locally (one-off):

```bash
# macOS with Homebrew
redis-server --port 6379 --save "" --appendonly no --daemonize yes
```

3. Run the example app that demonstrates tier-based limiting:

```bash
mvn -f example-hello/pom.xml spring-boot:run
```

4. In another terminal, run the included demo script to compare tiers:

```bash
./example-hello/demo_tier_test.sh free 15
./example-hello/demo_tier_test.sh pro 15
```

Each run will print the HTTP status and the `X-RateLimit-*` headers so you can observe different quotas for `free` vs `pro` tiers.

