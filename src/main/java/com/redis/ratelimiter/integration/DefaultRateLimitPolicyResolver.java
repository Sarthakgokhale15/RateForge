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
    private final java.util.Map<String, String> normalizedTierPolicies = new java.util.HashMap<>();

    public DefaultRateLimitPolicyResolver(RateLimiterProperties properties) {
        this.properties = properties;
        // normalize tier policy keys to support case-insensitive matching and flexible tier names
        LinkedHashMap<String, String> tierPolicies = properties.getWeb().getTierPolicies();
        if (tierPolicies != null) {
            for (Map.Entry<String, String> e : tierPolicies.entrySet()) {
                if (e.getKey() != null) {
                    normalizedTierPolicies.put(e.getKey().trim().toLowerCase(), e.getValue());
                }
            }
        }
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
                String lookup = tier.trim().toLowerCase();
                String policy = normalizedTierPolicies.get(lookup);
                if (policy != null && !policy.isBlank()) {
                    return policy;
                }
                // fallback: try direct lookup in original map (in case consumer used exact keys)
                String policyDirect = properties.getWeb().getTierPolicies().get(tier);
                if (policyDirect != null && !policyDirect.isBlank()) {
                    return policyDirect;
                }
            }
        }

        return properties.getWeb().getDefaultPolicyName();
    }
}
