package com.lifepill.user_auth.config;

import com.lifepill.user_auth.security.CustomUserDetailsService;
import com.lifepill.user_auth.security.JwtAuthenticationEntryPoint;
import com.lifepill.user_auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 * Configures JWT authentication, authorization rules, and security filters.
 * NOTE: CORS is handled by API Gateway only - no CORS config here.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomUserDetailsService userDetailsService;

    @Value("${api.version:v1}")
    private String apiVersion;

    /**
     * Swagger/OpenAPI endpoints.
     */
    private static final String[] SWAGGER_ENDPOINTS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    /**
     * Get public auth endpoints with API version prefix.
     */
    private String[] getPublicAuthEndpoints() {
        String prefix = "/" + apiVersion + "/user/auth";
        return new String[] {
                prefix + "/register",
                prefix + "/login",
                prefix + "/google",
                prefix + "/google/status",
                prefix + "/forgot-password",
                prefix + "/reset-password",
                prefix + "/verify-email",
                prefix + "/resend-verification",
                prefix + "/refresh-token"
        };
    }

    /**
     * Configure the security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] publicEndpoints = getPublicAuthEndpoints();
        String verifyEmailPath = "/" + apiVersion + "/user/auth/verify-email";
        String googleStatusPath = "/" + apiVersion + "/user/auth/google/status";
        
        http
                // CORS is handled by API Gateway - disabled here
                .cors(cors -> cors.disable())
                
                // Disable CSRF for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configure exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                
                // Stateless session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow public endpoints
                        .requestMatchers(HttpMethod.POST, publicEndpoints).permitAll()
                        .requestMatchers(HttpMethod.GET, verifyEmailPath).permitAll()
                        .requestMatchers(HttpMethod.GET, googleStatusPath).permitAll()
                        // Allow Swagger/OpenAPI endpoints
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        // Allow OPTIONS for CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Actuator endpoints (if needed)
                        .requestMatchers("/actuator/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Configure authentication provider
                .authenticationProvider(authenticationProvider())
                
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure authentication provider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configure authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configure password encoder with BCrypt (12 rounds).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
