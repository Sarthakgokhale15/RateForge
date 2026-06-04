package com.redis.ratelimiter.integration;

import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitKeyResolver {

    String resolveKey(HttpServletRequest request);
}
