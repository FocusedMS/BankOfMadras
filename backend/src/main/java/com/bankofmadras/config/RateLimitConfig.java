package com.bankofmadras.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        // OTP requests: 3 per hour
        if (key.startsWith("otp:")) {
            return Bucket4j.builder()
                    .addLimit(Bandwidth.simple(3, Duration.ofHours(1)))
                    .build();
        }
        // Login attempts: 5 per hour
        else if (key.startsWith("login:")) {
            return Bucket4j.builder()
                    .addLimit(Bandwidth.simple(5, Duration.ofHours(1)))
                    .build();
        }
        // Transaction requests: 10 per minute
        else if (key.startsWith("transaction:")) {
            return Bucket4j.builder()
                    .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                    .build();
        }
        // Default: 100 requests per minute
        else {
            return Bucket4j.builder()
                    .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))
                    .build();
        }
    }
} 