package com.redis.ratelimiter.model;

public class RateLimitPolicy {

    private AlgorithmType algorithm = AlgorithmType.TOKEN_BUCKET;
    private long limit = 100;
    private long windowSeconds = 60;
    private long capacity = 100;
    private double refillTokensPerSecond = 10.0;
    private double leakRatePerSecond = 10.0;
    private long requestedTokens = 1;

    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AlgorithmType algorithm) {
        this.algorithm = algorithm;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public double getRefillTokensPerSecond() {
        return refillTokensPerSecond;
    }

    public void setRefillTokensPerSecond(double refillTokensPerSecond) {
        this.refillTokensPerSecond = refillTokensPerSecond;
    }

    public double getLeakRatePerSecond() {
        return leakRatePerSecond;
    }

    public void setLeakRatePerSecond(double leakRatePerSecond) {
        this.leakRatePerSecond = leakRatePerSecond;
    }

    public long getRequestedTokens() {
        return requestedTokens;
    }

    public void setRequestedTokens(long requestedTokens) {
        this.requestedTokens = requestedTokens;
    }
}
