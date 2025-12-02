package com.lifepill.user_auth.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter configuration using Bucket4j.
 * Provides rate limiting for sensitive endpoints.
 */
@Component
public class RateLimiterConfig {

    @Value("${rate.limit.login.attempts}")
    private int loginAttempts;

    @Value("${rate.limit.login.duration-minutes}")
    private int loginDurationMinutes;

    @Value("${rate.limit.register.attempts}")
    private int registerAttempts;

    @Value("${rate.limit.register.duration-minutes}")
    private int registerDurationMinutes;

    @Value("${rate.limit.password-reset.attempts}")
    private int passwordResetAttempts;

    @Value("${rate.limit.password-reset.duration-minutes}")
    private int passwordResetDurationMinutes;

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> passwordResetBuckets = new ConcurrentHashMap<>();

    /**
     * Get or create rate limiter bucket for login attempts by IP.
     *
     * @param ip the client IP address
     * @return the rate limiter bucket
     */
    public Bucket getLoginBucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, k -> createBucket(loginAttempts, loginDurationMinutes));
    }

    /**
     * Get or create rate limiter bucket for registration attempts by IP.
     *
     * @param ip the client IP address
     * @return the rate limiter bucket
     */
    public Bucket getRegisterBucket(String ip) {
        return registerBuckets.computeIfAbsent(ip, k -> createBucket(registerAttempts, registerDurationMinutes));
    }

    /**
     * Get or create rate limiter bucket for password reset attempts by email.
     *
     * @param email the user email
     * @return the rate limiter bucket
     */
    public Bucket getPasswordResetBucket(String email) {
        return passwordResetBuckets.computeIfAbsent(email, k -> createBucket(passwordResetAttempts, passwordResetDurationMinutes));
    }

    /**
     * Create a new bucket with the specified capacity and refill duration.
     *
     * @param capacity the maximum number of tokens
     * @param refillMinutes the duration in minutes to refill all tokens
     * @return the created bucket
     */
    private Bucket createBucket(int capacity, int refillMinutes) {
        Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.intervally(capacity, Duration.ofMinutes(refillMinutes))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Check if a login attempt is allowed for the given IP.
     *
     * @param ip the client IP address
     * @return true if allowed, false if rate limited
     */
    public boolean allowLoginAttempt(String ip) {
        return getLoginBucket(ip).tryConsume(1);
    }

    /**
     * Check if a registration attempt is allowed for the given IP.
     *
     * @param ip the client IP address
     * @return true if allowed, false if rate limited
     */
    public boolean allowRegisterAttempt(String ip) {
        return getRegisterBucket(ip).tryConsume(1);
    }

    /**
     * Check if a password reset attempt is allowed for the given email.
     *
     * @param email the user email
     * @return true if allowed, false if rate limited
     */
    public boolean allowPasswordResetAttempt(String email) {
        return getPasswordResetBucket(email).tryConsume(1);
    }
}
