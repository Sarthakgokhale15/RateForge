package com.redis.ratelimiter.api.dto;

import com.redis.ratelimiter.model.AlgorithmType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RateLimitRequest {

    @NotBlank
    private String key;
    private String policyName;
    private AlgorithmType algorithm;

    @Min(1)
    private Long limit;

    @Min(1)
    private Long windowSeconds;

    @Min(1)
    private Long capacity;

    @Min(1)
    private Long requestedTokens;

    private Double refillTokensPerSecond;
    private Double leakRatePerSecond;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AlgorithmType algorithm) {
        this.algorithm = algorithm;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(Long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    public Long getRequestedTokens() {
        return requestedTokens;
    }

    public void setRequestedTokens(Long requestedTokens) {
        this.requestedTokens = requestedTokens;
    }

    public Double getRefillTokensPerSecond() {
        return refillTokensPerSecond;
    }

    public void setRefillTokensPerSecond(Double refillTokensPerSecond) {
        this.refillTokensPerSecond = refillTokensPerSecond;
    }

    public Double getLeakRatePerSecond() {
        return leakRatePerSecond;
    }

    public void setLeakRatePerSecond(Double leakRatePerSecond) {
        this.leakRatePerSecond = leakRatePerSecond;
    }
}
