package com.lifepill.user_auth.mapper;

import com.lifepill.user_auth.dto.request.AddressRequest;
import com.lifepill.user_auth.dto.response.AddressResponse;
import com.lifepill.user_auth.dto.response.AuthResponse;
import com.lifepill.user_auth.dto.response.ProfileData;
import com.lifepill.user_auth.dto.response.UserProfileResponse;
import com.lifepill.user_auth.entity.User;
import com.lifepill.user_auth.entity.UserAddress;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

/**
 * ModelMapper-based mapper for flexible object mapping.
 * Uses ModelMapper for runtime mapping with custom configurations.
 */
@Component
@RequiredArgsConstructor
public class UserModelMapper {

    private final ModelMapper modelMapper;

    /**
     * Convert AddressRequest DTO to UserAddress entity.
     *
     * @param request the address request
     * @return the user address entity
     */
    public UserAddress toUserAddress(AddressRequest request) {
        if (request == null) {
            return null;
        }
        UserAddress address = modelMapper.map(request, UserAddress.class);
        address.setIsPrimary(true);
        return address;
    }

    /**
     * Convert UserAddress entity to AddressResponse DTO.
     *
     * @param address the user address entity
     * @return the address response
     */
    public AddressResponse toAddressResponse(UserAddress address) {
        if (address == null) {
            return null;
        }
        return modelMapper.map(address, AddressResponse.class);
    }

    /**
     * Convert User entity to UserProfileResponse DTO.
     *
     * @param user the user entity
     * @return the user profile response
     */
    public UserProfileResponse toUserProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null)
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        UserAddress primaryAddress = user.getPrimaryAddress();
        if (primaryAddress != null) {
            response.setAddress(toAddressResponse(primaryAddress));
        }

        return response;
    }

    /**
     * Convert User entity to AuthResponse DTO with tokens.
     *
     * @param user the user entity
     * @param accessToken the access token
     * @param refreshToken the refresh token
     * @return the auth response
     */
    public AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        if (user == null) {
            return null;
        }

        ProfileData profileData = toProfileData(user);

        return AuthResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.getEmailVerified())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .profile(profileData)
                .build();
    }

    /**
     * Convert User entity to ProfileData DTO.
     *
     * @param user the user entity
     * @return the profile data
     */
    public ProfileData toProfileData(User user) {
        if (user == null) {
            return null;
        }

        return ProfileData.builder()
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null)
                .address(toAddressResponse(user.getPrimaryAddress()))
                .build();
    }

    /**
     * Update existing UserAddress from AddressRequest (partial update).
     *
     * @param request the address request
     * @param address the existing address to update
     */
    public void updateAddressFromRequest(AddressRequest request, UserAddress address) {
        if (request == null || address == null) {
            return;
        }

        if (request.getStreet() != null) {
            address.setStreet(request.getStreet());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            address.setZipCode(request.getZipCode());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
    }

    /**
     * Generic map method for flexible mapping.
     *
     * @param source the source object
     * @param destinationType the destination class
     * @param <S> source type
     * @param <D> destination type
     * @return the mapped object
     */
    public <S, D> D map(S source, Class<D> destinationType) {
        return source == null ? null : modelMapper.map(source, destinationType);
    }
}
