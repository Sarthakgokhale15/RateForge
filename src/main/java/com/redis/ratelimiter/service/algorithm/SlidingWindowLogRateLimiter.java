package com.redis.ratelimiter.service.algorithm;

import com.redis.ratelimiter.config.RateLimiterProperties;
import com.redis.ratelimiter.model.AlgorithmType;
import com.redis.ratelimiter.model.RateLimitPolicy;
import com.redis.ratelimiter.model.RateLimitResult;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.Objects;

@Component
public class SlidingWindowLogRateLimiter extends AbstractRedisRateLimiter implements RateLimiterAlgorithm {

    public SlidingWindowLogRateLimiter(StringRedisTemplate redisTemplate, RateLimiterProperties properties) {
        super(redisTemplate, properties);
    }

    @Override
    public AlgorithmType algorithmType() {
        return AlgorithmType.SLIDING_WINDOW_LOG;
    }

    @Override
    public RateLimitResult allow(String identityKey, RateLimitPolicy policy) {
        long windowMillis = Math.max(1, policy.getWindowSeconds()) * 1000;
        long limit = Math.max(1, policy.getLimit());
        long now = nowMillis();
        long cutoff = now - windowMillis;

        String redisKey = Objects.requireNonNull(key("sliding_log", identityKey));
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, cutoff);
        Long count = redisTemplate.opsForZSet().zCard(redisKey);
        long currentCount = count == null ? 0 : count;

        boolean allowed = currentCount < limit;
        long remaining = Math.max(0, limit - currentCount - (allowed ? 1 : 0));
        long retryAfter = 0;

        if (allowed) {
            redisTemplate.opsForZSet().add(redisKey, now + ":" + UUID.randomUUID(), now);
            redisTemplate.expire(redisKey, Objects.requireNonNull(Duration.ofMillis(windowMillis + 1000)));
        } else {
            retryAfter = Math.max(1, policy.getWindowSeconds());
        }

        return new RateLimitResult(allowed, remaining, retryAfter, algorithmType().name(), redisKey);
    }
}
