package com.redis.ratelimiter.model;

public record RateLimitResult(
        boolean allowed,
        long remaining,
        long retryAfterSeconds,
        String algorithm,
        String redisKey
) {
}
