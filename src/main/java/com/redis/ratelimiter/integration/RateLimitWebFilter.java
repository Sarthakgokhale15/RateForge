package com.redis.ratelimiter.integration;

import com.redis.ratelimiter.api.dto.RateLimitRequest;
import com.redis.ratelimiter.model.RateLimitResult;
import com.redis.ratelimiter.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RateLimitWebFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final RateLimitKeyResolver keyResolver;
    private final RateLimitPolicyResolver policyResolver;
    private final List<String> excludePaths;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public RateLimitWebFilter(
            RateLimiterService rateLimiterService,
            RateLimitKeyResolver keyResolver,
            RateLimitPolicyResolver policyResolver,
            List<String> excludePaths
    ) {
        this.rateLimiterService = rateLimiterService;
        this.keyResolver = keyResolver;
        this.policyResolver = policyResolver;
        this.excludePaths = excludePaths;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pathPattern : excludePaths) {
            if (matcher.match(Objects.requireNonNull(pathPattern), Objects.requireNonNull(uri))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    )
            throws ServletException, IOException {

        String policyName = policyResolver.resolvePolicyName(request);
        if (policyName == null || policyName.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRequest limitRequest = new RateLimitRequest();
        limitRequest.setKey(keyResolver.resolveKey(request));
        limitRequest.setPolicyName(policyName);

        RateLimitResult result = rateLimiterService.check(limitRequest);

        response.setHeader("X-RateLimit-Policy", policyName);
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        response.setHeader("X-RateLimit-Algorithm", result.algorithm());

        if (!result.allowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"allowed\":false,\"retryAfterSeconds\":" + result.retryAfterSeconds() + "}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
