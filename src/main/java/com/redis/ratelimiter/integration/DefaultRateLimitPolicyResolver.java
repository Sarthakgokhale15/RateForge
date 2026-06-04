package com.redis.ratelimiter.integration;

import com.redis.ratelimiter.config.RateLimiterProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultRateLimitPolicyResolver implements RateLimitPolicyResolver {

    private final RateLimiterProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public DefaultRateLimitPolicyResolver(RateLimiterProperties properties) {
        this.properties = properties;
    }

    @Override
    public String resolvePolicyName(HttpServletRequest request) {
        String path = request.getRequestURI();

        LinkedHashMap<String, String> pathPolicies = properties.getWeb().getPathPolicies();
        for (Map.Entry<String, String> entry : pathPolicies.entrySet()) {
            if (pathMatcher.match(Objects.requireNonNull(entry.getKey()), Objects.requireNonNull(path))) {
                return entry.getValue();
            }
        }

        String tierHeader = properties.getWeb().getTierHeader();
        if (tierHeader != null && !tierHeader.isBlank()) {
            String tier = request.getHeader(tierHeader);
            if (tier != null && !tier.isBlank()) {
                String policy = properties.getWeb().getTierPolicies().get(tier);
                if (policy != null && !policy.isBlank()) {
                    return policy;
                }
            }
        }

        return properties.getWeb().getDefaultPolicyName();
    }
}
