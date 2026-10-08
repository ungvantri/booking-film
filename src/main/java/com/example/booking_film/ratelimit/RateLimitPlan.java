package com.example.booking_film.ratelimit;

import io.github.bucket4j.Bandwidth;
import lombok.Getter;

import java.time.Duration;

@Getter
public enum RateLimitPlan {

    AUTH(15, Duration.ofMinutes(1)),

    GENERAL(60, Duration.ofMinutes(1));

    private final long capacity;
    private final Duration duration;

    RateLimitPlan(long capacity, Duration duration) {
        this.capacity = capacity;
        this.duration = duration;
    }

    public Bandwidth toBandwidth() {
        return Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, duration)
                .build();
    }

    public static RateLimitPlan resolvePlan(String uri) {
        if (uri.startsWith("/api/auth")) {
            return AUTH;
        }
        return GENERAL;
    }
}
