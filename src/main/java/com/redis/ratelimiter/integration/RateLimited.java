package com.redis.ratelimiter.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RateLimited {
    /**
     * Optional policy name to use for this endpoint. When empty, the policy resolver / default policy is used.
     */
    String policy() default "";
}
