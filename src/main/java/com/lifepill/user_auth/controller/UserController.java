package com.lifepill.user_auth.controller;

import com.lifepill.user_auth.dto.request.UpdateProfileRequest;
import com.lifepill.user_auth.dto.response.ApiResponse;
import com.lifepill.user_auth.dto.response.UserProfileResponse;
import com.lifepill.user_auth.security.UserPrincipal;
import com.lifepill.user_auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile endpoints.
 * Handles profile retrieval and updates.
 */
@Slf4j
@RestController
@RequestMapping("/${api.version:v1}/user")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Get current user's profile.
     *
     * @param userPrincipal the authenticated user
     * @return the user profile response
     */
    @Operation(
            summary = "Get user profile",
            description = "Retrieves the authenticated user's profile information including address"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        log.info("Get profile request for user: {}", userPrincipal.getId());
        UserProfileResponse profile = userService.getProfile(userPrincipal.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    /**
     * Update current user's profile.
     *
     * @param userPrincipal the authenticated user
     * @param request the update profile request
     * @return the updated user profile response
     */
    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's profile information. Supports partial updates."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("Update profile request for user: {}", userPrincipal.getId());
        UserProfileResponse profile = userService.updateProfile(userPrincipal.getId(), request);
        
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }
}
