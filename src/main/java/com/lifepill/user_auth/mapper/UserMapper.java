package com.lifepill.user_auth.mapper;

import com.lifepill.user_auth.dto.request.AddressRequest;
import com.lifepill.user_auth.dto.response.AddressResponse;
import com.lifepill.user_auth.dto.response.AuthResponse;
import com.lifepill.user_auth.dto.response.ProfileData;
import com.lifepill.user_auth.dto.response.UserProfileResponse;
import com.lifepill.user_auth.entity.User;
import com.lifepill.user_auth.entity.UserAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Unified mapper component that delegates to MapStruct and ModelMapper implementations.
 * This provides a single entry point for all mapping operations while allowing flexibility
 * to use either MapStruct (compile-time) or ModelMapper (runtime) based on requirements.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserMapStructMapper mapStructMapper;
    private final UserModelMapper modelMapper;

    /**
     * Convert AddressRequest DTO to UserAddress entity.
     * Uses MapStruct for efficient compile-time mapping.
     *
     * @param request the address request
     * @return the user address entity
     */
    public UserAddress toUserAddress(AddressRequest request) {
        return mapStructMapper.toUserAddress(request);
    }

    /**
     * Convert UserAddress entity to AddressResponse DTO.
     * Uses MapStruct for efficient compile-time mapping.
     *
     * @param address the user address entity
     * @return the address response
     */
    public AddressResponse toAddressResponse(UserAddress address) {
        return mapStructMapper.toAddressResponse(address);
    }

    /**
     * Convert User entity to UserProfileResponse DTO.
     * Uses ModelMapper for flexible runtime mapping.
     *
     * @param user the user entity
     * @return the user profile response
     */
    public UserProfileResponse toUserProfileResponse(User user) {
        return modelMapper.toUserProfileResponse(user);
    }

    /**
     * Convert User entity to AuthResponse DTO with tokens.
     * Uses ModelMapper for flexible runtime mapping.
     *
     * @param user the user entity
     * @param accessToken the access token
     * @param refreshToken the refresh token
     * @return the auth response
     */
    public AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        return modelMapper.toAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * Convert User entity to ProfileData DTO.
     * Uses MapStruct for efficient compile-time mapping.
     *
     * @param user the user entity
     * @return the profile data
     */
    public ProfileData toProfileData(User user) {
        return mapStructMapper.toProfileData(user);
    }

    /**
     * Update existing UserAddress from AddressRequest (partial update).
     * Uses ModelMapper for flexible runtime field updates.
     *
     * @param request the address request
     * @param address the existing address to update
     */
    public void updateAddressFromRequest(AddressRequest request, UserAddress address) {
        modelMapper.updateAddressFromRequest(request, address);
    }

    /**
     * Generic mapping method using ModelMapper.
     *
     * @param source the source object
     * @param destinationType the destination class
     * @param <S> source type
     * @param <D> destination type
     * @return the mapped object
     */
    public <S, D> D map(S source, Class<D> destinationType) {
        return modelMapper.map(source, destinationType);
    }
}
