package com.lifepill.user_auth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.lifepill.user_auth.dto.response.GoogleUserInfo;
import com.lifepill.user_auth.exception.InvalidTokenException;
import com.lifepill.user_auth.service.GoogleOAuthService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of GoogleOAuthService for verifying Google ID tokens.
 */
@Slf4j
@Service
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    @Value("${google.oauth.client-id.web:}")
    private String webClientId;

    @Value("${google.oauth.client-id.android:}")
    private String androidClientId;

    @Value("${google.oauth.client-id.ios:}")
    private String iosClientId;

    @Value("${google.oauth.enabled:false}")
    private boolean enabled;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() {
        if (enabled && hasValidClientId()) {
            List<String> clientIds = getClientIds();
            log.info("Initializing Google OAuth with {} client ID(s)", clientIds.size());

            verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(clientIds)
                    .build();
        } else {
            log.warn("Google OAuth is disabled or no client IDs configured");
        }
    }

    @Override
    public GoogleUserInfo verifyIdToken(String idToken) {
        if (!enabled) {
            throw new InvalidTokenException("Google Sign-In is not enabled");
        }

        if (verifier == null) {
            throw new InvalidTokenException("Google OAuth is not properly configured");
        }

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                log.warn("Invalid Google ID token provided");
                throw new InvalidTokenException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            // Validate email is verified by Google
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                log.warn("Google email not verified for: {}", payload.getEmail());
                throw new InvalidTokenException("Google email is not verified");
            }

            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");

            log.info("Successfully verified Google user: {}", email);

            return GoogleUserInfo.builder()
                    .googleId(googleId)
                    .email(email)
                    .firstName(firstName != null ? firstName : "")
                    .lastName(lastName != null ? lastName : "")
                    .pictureUrl(pictureUrl)
                    .emailVerified(true)
                    .build();

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying Google ID token: {}", e.getMessage(), e);
            throw new InvalidTokenException("Failed to verify Google ID token: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled && hasValidClientId();
    }

    private boolean hasValidClientId() {
        return isNotEmpty(webClientId) || isNotEmpty(androidClientId) || isNotEmpty(iosClientId);
    }

    private List<String> getClientIds() {
        return Arrays.asList(webClientId, androidClientId, iosClientId)
                .stream()
                .filter(this::isNotEmpty)
                .toList();
    }

    private boolean isNotEmpty(String str) {
        return str != null && !str.isBlank();
    }
}
