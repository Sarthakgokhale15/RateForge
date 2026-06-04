package com.redis.ratelimiter;

import com.redis.ratelimiter.config.RateLimiterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateForgeApplication.class, args);
    }
}
