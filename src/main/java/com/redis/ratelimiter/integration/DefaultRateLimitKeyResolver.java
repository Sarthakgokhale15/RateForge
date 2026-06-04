package com.redis.ratelimiter.integration;

import com.redis.ratelimiter.config.RateLimiterProperties;
import jakarta.servlet.http.HttpServletRequest;

public class DefaultRateLimitKeyResolver implements RateLimitKeyResolver {

    private final RateLimiterProperties properties;

    public DefaultRateLimitKeyResolver(RateLimiterProperties properties) {
        this.properties = properties;
    }

    @Override
    public String resolveKey(HttpServletRequest request) {
        String keyHeader = properties.getWeb().getKeyHeader();
        if (keyHeader != null && !keyHeader.isBlank()) {
            String headerValue = request.getHeader(keyHeader);
            if (headerValue != null && !headerValue.isBlank()) {
                return headerValue + "|" + request.getMethod() + "|" + request.getRequestURI();
            }
        }

        String ip = request.getRemoteAddr();
        if (ip == null || ip.isBlank()) {
            ip = "unknown-ip";
        }
        return ip + "|" + request.getMethod() + "|" + request.getRequestURI();
    }
}
