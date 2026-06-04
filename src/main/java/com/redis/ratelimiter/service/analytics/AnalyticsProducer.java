package com.redis.ratelimiter.service.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsProducer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String topic = "rateforge.events";

    public AnalyticsProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(AnalyticsEvent event) {
        try {
            String payload = mapper.writeValueAsString(event);
            kafkaTemplate.send(topic, event.getKey(), payload)
                    .whenComplete((meta, ex) -> {
                        if (ex != null) log.warn("Failed to publish analytics event", ex);
                    });
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize analytics event", e);
        }
    }
}
