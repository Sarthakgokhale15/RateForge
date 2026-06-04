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
public class SlidingWindowCounterRateLimiter extends AbstractRedisRateLimiter implements RateLimiterAlgorithm {

    public SlidingWindowCounterRateLimiter(StringRedisTemplate redisTemplate, RateLimiterProperties properties) {
        super(redisTemplate, properties);
    }

    @Override
    public AlgorithmType algorithmType() {
        return AlgorithmType.SLIDING_WINDOW_COUNTER;
    }

    @Override
    public RateLimitResult allow(String identityKey, RateLimitPolicy policy) {
        long window = Math.max(1, policy.getWindowSeconds());
        long limit = Math.max(1, policy.getLimit());
        long now = nowSeconds();

        long currentWindow = now / window;
        long previousWindow = currentWindow - 1;
        long elapsed = now % window;
        double previousWeight = (double) (window - elapsed) / window;

        String base = Objects.requireNonNull(key("sliding_counter", identityKey));
        String currentKey = base + ":" + currentWindow;
        String previousKey = base + ":" + previousWindow;

        long currentCount = parseLong(redisTemplate.opsForValue().get(currentKey), 0);
        long previousCount = parseLong(redisTemplate.opsForValue().get(previousKey), 0);

        double estimated = currentCount + (previousCount * previousWeight);
        boolean allowed = estimated < limit;
        long retryAfter = 0;

        if (allowed) {
            Long incremented = redisTemplate.opsForValue().increment(currentKey);
            if (incremented != null && incremented == 1) {
                redisTemplate.expire(currentKey, Objects.requireNonNull(Duration.ofSeconds(window * 2)));
            }
            currentCount = incremented == null ? currentCount : incremented;
            estimated = currentCount + (previousCount * previousWeight);
        } else {
            retryAfter = Math.max(1, window - elapsed);
        }

        long remaining = Math.max(0, (long) Math.floor(limit - estimated));
        return new RateLimitResult(allowed, remaining, retryAfter, algorithmType().name(), currentKey);
    }
}
