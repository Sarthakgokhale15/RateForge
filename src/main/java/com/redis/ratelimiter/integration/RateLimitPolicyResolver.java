package com.redis.ratelimiter.integration;

import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitPolicyResolver {

    String resolvePolicyName(HttpServletRequest request);
}
