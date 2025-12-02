package com.lifepill.user_auth.service;

import com.lifepill.user_auth.dto.response.GoogleUserInfo;

/**
 * Service interface for Google OAuth operations.
 */
public interface GoogleOAuthService {

    /**
     * Verify Google ID token and extract user information.
     *
     * @param idToken the Google ID token from client
     * @return GoogleUserInfo containing verified user data
     */
    GoogleUserInfo verifyIdToken(String idToken);

    /**
     * Check if Google OAuth is enabled.
     *
     * @return true if Google OAuth is configured and enabled
     */
    boolean isEnabled();
}
