package com.lifepill.user_auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing verified Google user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfo {

    private String googleId;
    private String email;
    private String firstName;
    private String lastName;
    private String pictureUrl;
    private Boolean emailVerified;
}
