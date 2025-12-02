package com.lifepill.user_auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Provides interactive API documentation at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI lifePillOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + contextPath)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.lifepill.com")
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    private Info apiInfo() {
        return new Info()
                .title("LifePill User Authentication API")
                .description("""
                        ## LifePill Mobile Application Authentication Service
                        
                        This API provides comprehensive authentication and user management functionality for the LifePill mobile application.
                        
                        ### Features:
                        - **User Registration** - Create new user accounts with email verification
                        - **Email/Password Login** - Traditional authentication with JWT tokens
                        - **Google Sign-In** - OAuth2 authentication via Google
                        - **Token Management** - Access and refresh token handling
                        - **Password Reset** - Secure password recovery via email
                        - **Email Verification** - Account verification workflow
                        - **Profile Management** - User profile and address management
                        
                        ### Authentication:
                        All protected endpoints require a valid JWT Bearer token in the Authorization header.
                        
                        ```
                        Authorization: Bearer <your_access_token>
                        ```
                        
                        ### Rate Limiting:
                        - Login: 5 attempts per 15 minutes per IP
                        - Registration: 3 attempts per hour per IP
                        - Password Reset: 3 attempts per hour per email
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("LifePill Development Team")
                        .email("dev@lifepill.com")
                        .url("https://lifepill.com"))
                .license(new License()
                        .name("Private License")
                        .url("https://lifepill.com/license"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT access token obtained from login or register endpoints");
    }
}
