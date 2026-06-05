package com.redis.ratelimiter.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "rate-limiter.kafka", name = "enabled", havingValue = "true")
public class KafkaProducerAutoConfiguration {

    private final RateLimiterProperties properties;

    public KafkaProducerAutoConfiguration(RateLimiterProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(name = "producerConfigs")
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        String servers = properties.getKafka() != null ? properties.getKafka().getBootstrapServers() : "localhost:9092";
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        return props;
    }

    @Bean
    @ConditionalOnMissingBean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    @ConditionalOnMissingBean
    public com.redis.ratelimiter.service.analytics.AnalyticsProducer analyticsProducer(
            java.util.Optional<KafkaTemplate<String, String>> kafkaTemplate,
            RateLimiterProperties properties
    ) {
        return new com.redis.ratelimiter.service.analytics.AnalyticsProducer(kafkaTemplate, properties);
    }
}
