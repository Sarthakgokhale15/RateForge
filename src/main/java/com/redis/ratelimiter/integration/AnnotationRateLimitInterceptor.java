package com.redis.ratelimiter.integration;

import com.redis.ratelimiter.api.dto.RateLimitRequest;
import com.redis.ratelimiter.config.RateLimiterProperties;
import com.redis.ratelimiter.model.RateLimitResult;
import com.redis.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class AnnotationRateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    private final RateLimitKeyResolver keyResolver;
    private final RateLimiterProperties properties;

    public AnnotationRateLimitInterceptor(RateLimiterService rateLimiterService,
                                          RateLimitKeyResolver keyResolver,
                                          RateLimiterProperties properties) {
        this.rateLimiterService = rateLimiterService;
        this.keyResolver = keyResolver;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        HandlerMethod hm = (HandlerMethod) handler;

        RateLimited ann = hm.getMethodAnnotation(RateLimited.class);
        if (ann == null) ann = hm.getBeanType().getAnnotation(RateLimited.class);
        if (ann == null) return true;

        String policyName = ann.policy();
        if (!StringUtils.hasText(policyName)) {
            policyName = properties.getWeb().getDefaultPolicyName();
        }

        RateLimitRequest limitRequest = new RateLimitRequest();
        limitRequest.setKey(keyResolver.resolveKey(request));
        limitRequest.setPolicyName(policyName);

        RateLimitResult result = rateLimiterService.check(limitRequest);

        response.setHeader("X-RateLimit-Policy", policyName);
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        response.setHeader("X-RateLimit-Algorithm", result.algorithm());

        if (!result.allowed()) {
            sendRejected(response, result);
            return false;
        }

        return true;
    }

    private void sendRejected(HttpServletResponse response, RateLimitResult result) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
        response.setContentType("application/json");
        response.getWriter().write("{\"allowed\":false,\"retryAfterSeconds\":" + result.retryAfterSeconds() + "}");
    }
}
