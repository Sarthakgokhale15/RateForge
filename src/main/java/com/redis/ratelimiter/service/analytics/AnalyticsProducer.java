package com.redis.ratelimiter.service.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redis.ratelimiter.config.RateLimiterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AnalyticsProducer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsProducer.class);
    private final Optional<KafkaTemplate<String, String>> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String topic;
    private final boolean enabled;

    public AnalyticsProducer(Optional<KafkaTemplate<String, String>> kafkaTemplate,
                             RateLimiterProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = properties.getKafka() != null ? properties.getKafka().getTopic() : "rateforge.events";
        this.enabled = properties.getKafka() != null && properties.getKafka().isEnabled();
    }

    public void publish(AnalyticsEvent event) {
        if (!enabled) return;
        if (kafkaTemplate.isEmpty()) {
            log.debug("KafkaTemplate not available; skipping analytics publish");
            return;
        }

        try {
            String payload = mapper.writeValueAsString(event);
            kafkaTemplate.get().send(topic, event.getKey(), payload)
                    .whenComplete((meta, ex) -> {
                        if (ex != null) log.warn("Failed to publish analytics event", ex);
                    });
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize analytics event", e);
        }
    }
}
