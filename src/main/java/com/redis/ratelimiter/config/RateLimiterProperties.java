package com.redis.ratelimiter.config;

import com.redis.ratelimiter.model.AlgorithmType;
import com.redis.ratelimiter.model.RateLimitPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private String keyPrefix = "drl";
    private AlgorithmType defaultAlgorithm = AlgorithmType.TOKEN_BUCKET;
    private Map<String, RateLimitPolicy> policies = new HashMap<>();
    private Web web = new Web();
    private Kafka kafka = new Kafka();

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public AlgorithmType getDefaultAlgorithm() {
        return defaultAlgorithm;
    }

    public void setDefaultAlgorithm(AlgorithmType defaultAlgorithm) {
        this.defaultAlgorithm = defaultAlgorithm;
    }

    public Map<String, RateLimitPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(Map<String, RateLimitPolicy> policies) {
        this.policies = policies;
    }

    public Web getWeb() {
        return web;
    }

    public void setWeb(Web web) {
        this.web = web;
    }

    public Kafka getKafka() { return kafka; }

    public void setKafka(Kafka kafka) { this.kafka = kafka; }

    public static class Web {
        private boolean enabled = true;
        private String defaultPolicyName = "api-default";
        private String tierHeader = "X-User-Tier";
        private String keyHeader = "X-User-Id";
        private LinkedHashMap<String, String> tierPolicies = new LinkedHashMap<>();
        private LinkedHashMap<String, String> pathPolicies = new LinkedHashMap<>();
        private List<String> excludePaths = List.of("/actuator/**");

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDefaultPolicyName() {
            return defaultPolicyName;
        }

        public void setDefaultPolicyName(String defaultPolicyName) {
            this.defaultPolicyName = defaultPolicyName;
        }

        public String getTierHeader() {
            return tierHeader;
        }

        public void setTierHeader(String tierHeader) {
            this.tierHeader = tierHeader;
        }

        public String getKeyHeader() {
            return keyHeader;
        }

        public void setKeyHeader(String keyHeader) {
            this.keyHeader = keyHeader;
        }

        public LinkedHashMap<String, String> getTierPolicies() {
            return tierPolicies;
        }

        public void setTierPolicies(LinkedHashMap<String, String> tierPolicies) {
            this.tierPolicies = tierPolicies;
        }

        public LinkedHashMap<String, String> getPathPolicies() {
            return pathPolicies;
        }

        public void setPathPolicies(LinkedHashMap<String, String> pathPolicies) {
            this.pathPolicies = pathPolicies;
        }

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }
    }

    public static class Kafka {
        private boolean enabled = true;
        private String bootstrapServers = "kafka:9092";
        private String topic = "rateforge.events";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getBootstrapServers() { return bootstrapServers; }
        public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
    }
}
