package com.lifepill.user_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
 */
@SpringBootApplication
public class UserAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserAuthApplication.class, args);
	}

}
