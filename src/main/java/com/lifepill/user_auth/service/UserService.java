package com.lifepill.user_auth.service;

import com.lifepill.user_auth.dto.request.UpdateProfileRequest;
import com.lifepill.user_auth.dto.response.UserProfileResponse;

import java.util.UUID;

/**
 * Service interface for user profile operations.
 * Defines the contract for user management business logic.
 */
public interface UserService {

    /**
     * Get user profile by user ID.
     *
     * @param userId the user ID
     * @return the user profile response
     */
    UserProfileResponse getProfile(UUID userId);

    /**
     * Update user profile.
     *
     * @param userId the user ID
     * @param request the update profile request
     * @return the updated user profile response
     */
    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    /**
     * Delete user account.
     * This will permanently delete the user and all associated data.
     *
     * @param userId the user ID
     */
    void deleteAccount(UUID userId);
}
