package com.lifepill.user_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the LifePill User Authentication Service.
 * This service provides JWT-based authentication for the LifePill mobile application.
 *
 * Features:
 * - User registration with email verification
 * - JWT-based authentication with access and refresh tokens
 * - Password reset functionality
 * - User profile management
 * - Rate limiting for security
 * - CORS configuration for mobile apps
 * - Eureka service discovery integration
 * - Centralized configuration via Config Server
 * - Actuator endpoints for monitoring
 * 
 * Start Order: Service Registry -> Config Server -> User Auth Service
 * 
 * @author LifePill Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserAuthApplication.class, args);
	}

}
