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
public class RateLimitingConfig {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }

    public Bucket resolveBucket(String key, int tokens, Duration duration) {
        return buckets.computeIfAbsent(key, k -> createNewBucket(tokens, duration));
    }

    private Bucket createNewBucket(int tokens, Duration duration) {
        Bandwidth limit = Bandwidth.classic(tokens, Refill.intervally(tokens, duration));
        return Bucket4j.builder().addLimit(limit).build();
    }
} 