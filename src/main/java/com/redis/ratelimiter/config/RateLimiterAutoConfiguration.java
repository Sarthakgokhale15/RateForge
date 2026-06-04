package com.redis.ratelimiter.config;

import com.redis.ratelimiter.integration.DefaultRateLimitKeyResolver;
import com.redis.ratelimiter.integration.DefaultRateLimitPolicyResolver;
import com.redis.ratelimiter.integration.RateLimitKeyResolver;
import com.redis.ratelimiter.integration.RateLimitPolicyResolver;
import com.redis.ratelimiter.integration.RateLimitWebFilter;
import com.redis.ratelimiter.service.NodeInfoProvider;
import com.redis.ratelimiter.service.RateLimiterService;
import com.redis.ratelimiter.service.algorithm.FixedWindowRateLimiter;
import com.redis.ratelimiter.service.algorithm.LeakyBucketRateLimiter;
import com.redis.ratelimiter.service.algorithm.RateLimiterAlgorithm;
import com.redis.ratelimiter.service.algorithm.SlidingWindowCounterRateLimiter;
import com.redis.ratelimiter.service.algorithm.SlidingWindowLogRateLimiter;
import com.redis.ratelimiter.service.algorithm.TokenBucketRateLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FixedWindowRateLimiter fixedWindowRateLimiter(StringRedisTemplate redisTemplate, RateLimiterProperties properties) {
        return new FixedWindowRateLimiter(redisTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SlidingWindowLogRateLimiter slidingWindowLogRateLimiter(StringRedisTemplate redisTemplate, RateLimiterProperties properties) {
        return new SlidingWindowLogRateLimiter(redisTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SlidingWindowCounterRateLimiter slidingWindowCounterRateLimiter(StringRedisTemplate redisTemplate, RateLimiterProperties properties) {
        return new SlidingWindowCounterRateLimiter(redisTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenBucketRateLimiter tokenBucketRateLimiter(
            StringRedisTemplate redisTemplate,
            RateLimiterProperties properties,
            @Qualifier("tokenBucketScript") RedisScript<String> tokenBucketScript
    ) {
        return new TokenBucketRateLimiter(redisTemplate, properties, tokenBucketScript);
    }

    @Bean
    @ConditionalOnMissingBean
    public LeakyBucketRateLimiter leakyBucketRateLimiter(
            StringRedisTemplate redisTemplate,
            RateLimiterProperties properties,
            @Qualifier("leakyBucketScript") RedisScript<String> leakyBucketScript
    ) {
        return new LeakyBucketRateLimiter(redisTemplate, properties, leakyBucketScript);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterService rateLimiterService(List<RateLimiterAlgorithm> algorithms, RateLimiterProperties properties) {
        return new RateLimiterService(algorithms, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public NodeInfoProvider nodeInfoProvider() {
        return new NodeInfoProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitKeyResolver rateLimitKeyResolver(RateLimiterProperties properties) {
        return new DefaultRateLimitKeyResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitPolicyResolver rateLimitPolicyResolver(RateLimiterProperties properties) {
        return new DefaultRateLimitPolicyResolver(properties);
    }

    @Bean
    @ConditionalOnClass(FilterRegistrationBean.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "rate-limiter.web", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "rateLimitFilterRegistration")
    public FilterRegistrationBean<RateLimitWebFilter> rateLimitFilterRegistration(
            RateLimiterService rateLimiterService,
            RateLimitKeyResolver keyResolver,
            RateLimitPolicyResolver policyResolver,
            RateLimiterProperties properties
    ) {
        RateLimitWebFilter filter = new RateLimitWebFilter(
                rateLimiterService,
                keyResolver,
                policyResolver,
                properties.getWeb().getExcludePaths()
        );

        FilterRegistrationBean<RateLimitWebFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(-100);
        registration.setName("distributedRateLimiterFilter");
        registration.addUrlPatterns("/*");
        return registration;
    }
}
