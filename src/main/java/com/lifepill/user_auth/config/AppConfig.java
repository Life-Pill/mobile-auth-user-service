package com.lifepill.user_auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application configuration for async and scheduled tasks.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig {
    // Additional bean configurations can be added here
}
