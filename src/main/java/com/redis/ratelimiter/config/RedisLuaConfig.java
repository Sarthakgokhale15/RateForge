package com.redis.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisLuaConfig {

    @Bean(name = "tokenBucketScript")
    public RedisScript<String> tokenBucketScript() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
        script.setResultType(String.class);
        return script;
    }

    @Bean(name = "leakyBucketScript")
    public RedisScript<String> leakyBucketScript() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/leaky_bucket.lua"));
        script.setResultType(String.class);
        return script;
    }
}
