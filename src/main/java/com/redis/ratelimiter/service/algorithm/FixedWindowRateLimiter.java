package com.redis.ratelimiter.service.algorithm;

import com.redis.ratelimiter.config.RateLimiterProperties;
import com.redis.ratelimiter.model.AlgorithmType;
import com.redis.ratelimiter.model.RateLimitPolicy;
import com.redis.ratelimiter.model.RateLimitResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public class FixedWindowRateLimiter extends AbstractRedisRateLimiter implements RateLimiterAlgorithm {

    public FixedWindowRateLimiter(StringRedisTemplate redisTemplate, RateLimiterProperties properties) {
        super(redisTemplate, properties);
    }

    @Override
    public AlgorithmType algorithmType() {
        return AlgorithmType.FIXED_WINDOW;
    }

    @Override
    public RateLimitResult allow(String identityKey, RateLimitPolicy policy) {
        long nowSeconds = nowSeconds();
        long window = Math.max(1, policy.getWindowSeconds());
        long limit = Math.max(1, policy.getLimit());
        long bucket = nowSeconds / window;

        String redisKey = Objects.requireNonNull(key("fixed", identityKey)) + ":" + bucket;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, Objects.requireNonNull(Duration.ofSeconds(window + 1)));
        }

        long currentCount = count == null ? 0 : count;
        boolean allowed = currentCount <= limit;
        long remaining = Math.max(0, limit - currentCount);
        long retryAfter = allowed ? 0 : Math.max(1, (bucket + 1) * window - nowSeconds);

        return new RateLimitResult(allowed, remaining, retryAfter, algorithmType().name(), redisKey);
    }
}
