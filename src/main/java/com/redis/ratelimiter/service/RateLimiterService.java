package com.redis.ratelimiter.service;

import com.redis.ratelimiter.api.dto.RateLimitRequest;
import com.redis.ratelimiter.config.RateLimiterProperties;
import com.redis.ratelimiter.model.AlgorithmType;
import com.redis.ratelimiter.model.RateLimitPolicy;
import com.redis.ratelimiter.model.RateLimitResult;
import com.redis.ratelimiter.service.algorithm.RateLimiterAlgorithm;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class RateLimiterService {

    private final Map<AlgorithmType, RateLimiterAlgorithm> algorithms = new EnumMap<>(AlgorithmType.class);
    private final RateLimiterProperties properties;

    public RateLimiterService(List<RateLimiterAlgorithm> algorithmBeans, RateLimiterProperties properties) {
        this.properties = properties;
        algorithmBeans.forEach(algo -> this.algorithms.put(algo.algorithmType(), algo));
    }

    public RateLimitResult check(RateLimitRequest request) {
        RateLimitPolicy policy = resolvePolicy(request);
        AlgorithmType algorithm = policy.getAlgorithm() == null ? properties.getDefaultAlgorithm() : policy.getAlgorithm();

        RateLimiterAlgorithm engine = algorithms.get(algorithm);
        if (engine == null) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
        return engine.allow(request.getKey(), policy);
    }

    private RateLimitPolicy resolvePolicy(RateLimitRequest request) {
        RateLimitPolicy basePolicy = new RateLimitPolicy();
        basePolicy.setAlgorithm(properties.getDefaultAlgorithm());

        if (request.getPolicyName() != null && !request.getPolicyName().isBlank()) {
            RateLimitPolicy configured = properties.getPolicies().get(request.getPolicyName());
            if (configured == null) {
                throw new IllegalArgumentException("Unknown policyName: " + request.getPolicyName());
            }
            basePolicy = copy(configured);
        }

        if (request.getAlgorithm() != null) {
            basePolicy.setAlgorithm(request.getAlgorithm());
        }
        if (request.getLimit() != null) {
            basePolicy.setLimit(request.getLimit());
        }
        if (request.getWindowSeconds() != null) {
            basePolicy.setWindowSeconds(request.getWindowSeconds());
        }
        if (request.getCapacity() != null) {
            basePolicy.setCapacity(request.getCapacity());
        }
        if (request.getRequestedTokens() != null) {
            basePolicy.setRequestedTokens(request.getRequestedTokens());
        }
        if (request.getRefillTokensPerSecond() != null) {
            basePolicy.setRefillTokensPerSecond(request.getRefillTokensPerSecond());
        }
        if (request.getLeakRatePerSecond() != null) {
            basePolicy.setLeakRatePerSecond(request.getLeakRatePerSecond());
        }

        return basePolicy;
    }

    private RateLimitPolicy copy(RateLimitPolicy source) {
        RateLimitPolicy target = new RateLimitPolicy();
        target.setAlgorithm(source.getAlgorithm());
        target.setLimit(source.getLimit());
        target.setWindowSeconds(source.getWindowSeconds());
        target.setCapacity(source.getCapacity());
        target.setRequestedTokens(source.getRequestedTokens());
        target.setRefillTokensPerSecond(source.getRefillTokensPerSecond());
        target.setLeakRatePerSecond(source.getLeakRatePerSecond());
        return target;
    }
}
