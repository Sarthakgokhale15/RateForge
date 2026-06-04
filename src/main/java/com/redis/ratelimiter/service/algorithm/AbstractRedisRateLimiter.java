package com.redis.ratelimiter.service.algorithm;

import com.redis.ratelimiter.config.RateLimiterProperties;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;

public abstract class AbstractRedisRateLimiter {

    protected final StringRedisTemplate redisTemplate;
    protected final RateLimiterProperties properties;

    protected AbstractRedisRateLimiter(StringRedisTemplate redisTemplate, RateLimiterProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    protected String key(String algorithm, String identityKey) {
        return properties.getKeyPrefix() + ":" + algorithm + ":" + identityKey;
    }

    protected long nowMillis() {
        return Instant.now().toEpochMilli();
    }

    protected long nowSeconds() {
        return Instant.now().getEpochSecond();
    }

    protected long parseLong(String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected double parseDouble(String value, double defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
