package com.redis.ratelimiter.service.algorithm;

import com.redis.ratelimiter.config.RateLimiterProperties;
import com.redis.ratelimiter.model.AlgorithmType;
import com.redis.ratelimiter.model.RateLimitPolicy;
import com.redis.ratelimiter.model.RateLimitResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;

@Component
public class TokenBucketRateLimiter extends AbstractRedisRateLimiter implements RateLimiterAlgorithm {

    private final RedisScript<String> tokenBucketScript;

    public TokenBucketRateLimiter(
            StringRedisTemplate redisTemplate,
            RateLimiterProperties properties,
            @Qualifier("tokenBucketScript") RedisScript<String> tokenBucketScript
    ) {
        super(redisTemplate, properties);
        this.tokenBucketScript = tokenBucketScript;
    }

    @Override
    public AlgorithmType algorithmType() {
        return AlgorithmType.TOKEN_BUCKET;
    }

    @Override
    @SuppressWarnings("null")
    public RateLimitResult allow(String identityKey, RateLimitPolicy policy) {
        long capacity = Math.max(1, policy.getCapacity());
        double refillRate = Math.max(0.0001d, policy.getRefillTokensPerSecond());
        long requestedTokens = Math.max(1, policy.getRequestedTokens());
        long now = nowMillis();
        long ttl = Math.max(2, policy.getWindowSeconds() * 2);

        String redisKey = Objects.requireNonNull(key("token_bucket", identityKey));
        String result = redisTemplate.execute(
            Objects.requireNonNull(tokenBucketScript),
                Collections.singletonList(redisKey),
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(requestedTokens),
                String.valueOf(now),
                String.valueOf(ttl)
        );

        String[] parts = result == null ? new String[]{"0", "0", "1"} : result.split(":");
        boolean allowed = "1".equals(parts[0]);
        long remaining = parts.length > 1 ? parseLong(parts[1], 0) : 0;
        long retryAfter = parts.length > 2 ? parseLong(parts[2], 1) : 1;

        return new RateLimitResult(allowed, remaining, allowed ? 0 : retryAfter, algorithmType().name(), redisKey);
    }
}
