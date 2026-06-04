package com.redis.ratelimiter.service.analytics;

import java.time.Instant;

public class AnalyticsEvent {
    private String key;
    private boolean allowed;
    private long remaining;
    private long retryAfterSeconds;
    private String algorithm;
    private String policyName;
    private String nodeId;
    private Instant timestamp;

    public AnalyticsEvent() {}

    public AnalyticsEvent(String key, boolean allowed, long remaining, long retryAfterSeconds, String algorithm, String policyName, String nodeId) {
        this.key = key;
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfterSeconds = retryAfterSeconds;
        this.algorithm = algorithm;
        this.policyName = policyName;
        this.nodeId = nodeId;
        this.timestamp = Instant.now();
    }

    public String getKey() { return key; }
    public boolean isAllowed() { return allowed; }
    public long getRemaining() { return remaining; }
    public long getRetryAfterSeconds() { return retryAfterSeconds; }
    public String getAlgorithm() { return algorithm; }
    public String getPolicyName() { return policyName; }
    public String getNodeId() { return nodeId; }
    public Instant getTimestamp() { return timestamp; }

    public void setKey(String key) { this.key = key; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }
    public void setRemaining(long remaining) { this.remaining = remaining; }
    public void setRetryAfterSeconds(long retryAfterSeconds) { this.retryAfterSeconds = retryAfterSeconds; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
