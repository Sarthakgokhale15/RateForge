package com.redis.ratelimiter.service.algorithm;

import com.redis.ratelimiter.model.AlgorithmType;
import com.redis.ratelimiter.model.RateLimitPolicy;
import com.redis.ratelimiter.model.RateLimitResult;

public interface RateLimiterAlgorithm {

    AlgorithmType algorithmType();

    RateLimitResult allow(String identityKey, RateLimitPolicy policy);
}
