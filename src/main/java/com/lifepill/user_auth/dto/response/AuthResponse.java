package com.lifepill.user_auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses (register/login).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean emailVerified;
    private String accessToken;
    private String refreshToken;
    private ProfileData profile;
}
