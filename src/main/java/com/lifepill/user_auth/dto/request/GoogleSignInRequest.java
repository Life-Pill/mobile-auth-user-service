package com.lifepill.user_auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Google Sign-In.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSignInRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;

    private String deviceId;
}
