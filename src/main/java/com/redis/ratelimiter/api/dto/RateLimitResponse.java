package com.redis.ratelimiter.api.dto;

public record RateLimitResponse(
        boolean allowed,
        long remaining,
        long retryAfterSeconds,
        String algorithm,
        String redisKey,
        String nodeId
) {
}
