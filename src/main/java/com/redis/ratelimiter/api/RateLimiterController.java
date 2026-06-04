package com.redis.ratelimiter.api;

import com.redis.ratelimiter.api.dto.RateLimitRequest;
import com.redis.ratelimiter.api.dto.RateLimitResponse;
import com.redis.ratelimiter.model.RateLimitResult;
import com.redis.ratelimiter.service.NodeInfoProvider;
import com.redis.ratelimiter.service.RateLimiterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/limiter")
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;
    private final NodeInfoProvider nodeInfoProvider;

    public RateLimiterController(RateLimiterService rateLimiterService, NodeInfoProvider nodeInfoProvider) {
        this.rateLimiterService = rateLimiterService;
        this.nodeInfoProvider = nodeInfoProvider;
    }

    @PostMapping("/check")
    public ResponseEntity<RateLimitResponse> check(@Valid @RequestBody RateLimitRequest request) {
        RateLimitResult result = rateLimiterService.check(request);

        RateLimitResponse response = new RateLimitResponse(
                result.allowed(),
                result.remaining(),
                result.retryAfterSeconds(),
                result.algorithm(),
                result.redisKey(),
                nodeInfoProvider.currentNodeId()
        );

        return ResponseEntity.status(result.allowed() ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
