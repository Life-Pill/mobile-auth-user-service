package com.lifepill.user_auth.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter configuration using Bucket4j.
 * Provides dual-layer rate limiting (IP + User/Email) for sensitive endpoints.
 * 
 * <p>Rate limiting strategy:</p>
 * <ul>
 *   <li><b>IP-based:</b> Prevents brute force from a single source</li>
 *   <li><b>User-based:</b> Prevents distributed attacks on a specific account</li>
 * </ul>
 * 
 * <p>A request is allowed only if BOTH IP and user limits are not exceeded.</p>
 */
@Slf4j
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

    // User-based limits (more lenient than IP limits)
    @Value("${rate.limit.login.user-attempts:10}")
    private int loginUserAttempts;

    @Value("${rate.limit.register.email-attempts:5}")
    private int registerEmailAttempts;

    // IP-based buckets
    private final Map<String, BucketWrapper> loginIpBuckets = new ConcurrentHashMap<>();
    private final Map<String, BucketWrapper> registerIpBuckets = new ConcurrentHashMap<>();
    
    // User/Email-based buckets
    private final Map<String, BucketWrapper> loginUserBuckets = new ConcurrentHashMap<>();
    private final Map<String, BucketWrapper> registerEmailBuckets = new ConcurrentHashMap<>();
    private final Map<String, BucketWrapper> passwordResetBuckets = new ConcurrentHashMap<>();

    /**
     * Wrapper class to track bucket creation time for cleanup.
     */
    private static class BucketWrapper {
        final Bucket bucket;
        final Instant createdAt;

        BucketWrapper(Bucket bucket) {
            this.bucket = bucket;
            this.createdAt = Instant.now();
        }

        boolean isExpired(Duration maxAge) {
            return Instant.now().isAfter(createdAt.plus(maxAge));
        }
    }

    /**
     * Check if a login attempt is allowed using dual-layer rate limiting.
     * Both IP and email/user limits must pass.
     *
     * @param ip the client IP address
     * @param email the user email attempting login
     * @return true if allowed, false if rate limited
     */
    public boolean allowLoginAttempt(String ip, String email) {
        boolean ipAllowed = getLoginIpBucket(ip).tryConsume(1);
        boolean userAllowed = getLoginUserBucket(email).tryConsume(1);
        
        if (!ipAllowed) {
            log.warn("Login rate limit exceeded for IP: {}", maskIp(ip));
        }
        if (!userAllowed) {
            log.warn("Login rate limit exceeded for user: {}", maskEmail(email));
        }
        
        return ipAllowed && userAllowed;
    }

    /**
     * Get or create IP-based rate limiter bucket for login attempts.
     */
    private Bucket getLoginIpBucket(String ip) {
        return loginIpBuckets.computeIfAbsent(ip, 
            k -> new BucketWrapper(createBucket(loginAttempts, loginDurationMinutes))).bucket;
    }

    /**
     * Get or create user-based rate limiter bucket for login attempts.
     * More lenient than IP-based to avoid blocking legitimate users on shared networks.
     */
    private Bucket getLoginUserBucket(String email) {
        return loginUserBuckets.computeIfAbsent(email.toLowerCase(), 
            k -> new BucketWrapper(createBucket(loginUserAttempts, loginDurationMinutes))).bucket;
    }

    /**
     * Check if a registration attempt is allowed using dual-layer rate limiting.
     * Both IP and email limits must pass.
     *
     * @param ip the client IP address
     * @param email the email being registered
     * @return true if allowed, false if rate limited
     */
    public boolean allowRegisterAttempt(String ip, String email) {
        boolean ipAllowed = getRegisterIpBucket(ip).tryConsume(1);
        boolean emailAllowed = getRegisterEmailBucket(email).tryConsume(1);
        
        if (!ipAllowed) {
            log.warn("Registration rate limit exceeded for IP: {}", maskIp(ip));
        }
        if (!emailAllowed) {
            log.warn("Registration rate limit exceeded for email: {}", maskEmail(email));
        }
        
        return ipAllowed && emailAllowed;
    }

    /**
     * Get or create IP-based rate limiter bucket for registration attempts.
     */
    private Bucket getRegisterIpBucket(String ip) {
        return registerIpBuckets.computeIfAbsent(ip, 
            k -> new BucketWrapper(createBucket(registerAttempts, registerDurationMinutes))).bucket;
    }

    /**
     * Get or create email-based rate limiter bucket for registration attempts.
     * Prevents repeated registration attempts with the same email.
     */
    private Bucket getRegisterEmailBucket(String email) {
        return registerEmailBuckets.computeIfAbsent(email.toLowerCase(), 
            k -> new BucketWrapper(createBucket(registerEmailAttempts, registerDurationMinutes))).bucket;
    }

    /**
     * Check if a password reset attempt is allowed.
     * Uses both IP and email-based limiting.
     *
     * @param ip the client IP address
     * @param email the user email
     * @return true if allowed, false if rate limited
     */
    public boolean allowPasswordResetAttempt(String ip, String email) {
        boolean ipAllowed = getLoginIpBucket(ip).tryConsume(1); // Reuse login IP bucket
        boolean emailAllowed = getPasswordResetBucket(email).tryConsume(1);
        
        if (!ipAllowed) {
            log.warn("Password reset rate limit exceeded for IP: {}", maskIp(ip));
        }
        if (!emailAllowed) {
            log.warn("Password reset rate limit exceeded for email: {}", maskEmail(email));
        }
        
        return ipAllowed && emailAllowed;
    }

    /**
     * Get or create rate limiter bucket for password reset attempts by email.
     */
    private Bucket getPasswordResetBucket(String email) {
        return passwordResetBuckets.computeIfAbsent(email.toLowerCase(), 
            k -> new BucketWrapper(createBucket(passwordResetAttempts, passwordResetDurationMinutes))).bucket;
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
     * Get remaining attempts for login by IP.
     *
     * @param ip the client IP address
     * @return remaining attempts
     */
    public long getRemainingLoginAttempts(String ip) {
        return getLoginIpBucket(ip).getAvailableTokens();
    }

    /**
     * Get remaining attempts for registration by IP.
     *
     * @param ip the client IP address
     * @return remaining attempts
     */
    public long getRemainingRegisterAttempts(String ip) {
        return getRegisterIpBucket(ip).getAvailableTokens();
    }

    /**
     * Mask IP address for logging (privacy protection).
     */
    private String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) return "unknown";
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".***";
        }
        return "***";
    }

    /**
     * Mask email for logging (privacy protection).
     */
    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) return "unknown";
        int atIndex = email.indexOf('@');
        if (atIndex > 2) {
            return email.substring(0, 2) + "***" + email.substring(atIndex);
        }
        return "***" + email.substring(atIndex);
    }

    /**
     * Periodically clean up expired buckets to prevent memory leaks.
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredBuckets() {
        Duration maxAge = Duration.ofHours(24);
        
        int removed = cleanupMap(loginIpBuckets, maxAge);
        removed += cleanupMap(loginUserBuckets, maxAge);
        removed += cleanupMap(registerIpBuckets, maxAge);
        removed += cleanupMap(registerEmailBuckets, maxAge);
        removed += cleanupMap(passwordResetBuckets, maxAge);
        
        if (removed > 0) {
            log.info("Cleaned up {} expired rate limit buckets", removed);
        }
    }

    private int cleanupMap(Map<String, BucketWrapper> map, Duration maxAge) {
        int initialSize = map.size();
        map.entrySet().removeIf(entry -> entry.getValue().isExpired(maxAge));
        return initialSize - map.size();
    }

    /**
     * @deprecated Use {@link #allowLoginAttempt(String, String)} instead for dual-layer limiting.
     */
    @Deprecated
    public boolean allowLoginAttempt(String ip) {
        return getLoginIpBucket(ip).tryConsume(1);
    }

    /**
     * @deprecated Use {@link #allowRegisterAttempt(String, String)} instead for dual-layer limiting.
     */
    @Deprecated
    public boolean allowRegisterAttempt(String ip) {
        return getRegisterIpBucket(ip).tryConsume(1);
    }

    /**
     * @deprecated Use {@link #allowPasswordResetAttempt(String, String)} instead for dual-layer limiting.
     */
    @Deprecated
    public boolean allowPasswordResetAttempt(String email) {
        return getPasswordResetBucket(email).tryConsume(1);
    }
}
