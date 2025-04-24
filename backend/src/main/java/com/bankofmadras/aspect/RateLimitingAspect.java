package com.bankofmadras.aspect;

import com.bankofmadras.config.RateLimitingConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Aspect
@Component
public class RateLimitingAspect {
    private final RateLimitingConfig rateLimitingConfig;

    public RateLimitingAspect(RateLimitingConfig rateLimitingConfig) {
        this.rateLimitingConfig = rateLimitingConfig;
    }

    @Around("@annotation(com.bankofmadras.annotation.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();
        String endpoint = request.getRequestURI();

        // Different rate limits for different endpoints
        Bucket bucket;
        if (endpoint.contains("/auth/request-otp")) {
            // 3 requests per hour for OTP
            bucket = rateLimitingConfig.resolveBucket(ip + "_otp", 3, Duration.ofHours(1));
        } else if (endpoint.contains("/auth/login")) {
            // 5 requests per hour for login
            bucket = rateLimitingConfig.resolveBucket(ip + "_login", 5, Duration.ofHours(1));
        } else if (endpoint.contains("/transactions")) {
            // 10 requests per hour for transactions
            bucket = rateLimitingConfig.resolveBucket(ip + "_transactions", 10, Duration.ofHours(1));
        } else {
            // Default: 20 requests per hour
            bucket = rateLimitingConfig.resolveBucket(ip + "_default", 20, Duration.ofHours(1));
        }

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        }
    }
} 