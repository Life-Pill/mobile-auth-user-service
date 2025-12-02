package com.lifepill.user_auth.mapper;

import com.lifepill.user_auth.dto.request.AddressRequest;
import com.lifepill.user_auth.dto.request.RegisterRequest;
import com.lifepill.user_auth.dto.response.AddressResponse;
import com.lifepill.user_auth.dto.response.AuthResponse;
import com.lifepill.user_auth.dto.response.ProfileData;
import com.lifepill.user_auth.dto.response.UserProfileResponse;
import com.lifepill.user_auth.entity.User;
import com.lifepill.user_auth.entity.UserAddress;
import org.mapstruct.*;

/**
 * MapStruct mapper interface for User entity mappings.
 * Provides compile-time type-safe mappings between entities and DTOs.
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserMapStructMapper {

    /**
     * Map AddressRequest to UserAddress entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isPrimary", constant = "true")
    UserAddress toUserAddress(AddressRequest request);

    /**
     * Map UserAddress entity to AddressResponse.
     */
    AddressResponse toAddressResponse(UserAddress address);

    /**
     * Map User entity to UserProfileResponse.
     */
    @Mapping(target = "userId", expression = "java(user.getId().toString())")
    @Mapping(target = "dateOfBirth", expression = "java(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null)")
    @Mapping(target = "address", expression = "java(toAddressResponse(user.getPrimaryAddress()))")
    UserProfileResponse toUserProfileResponse(User user);

    /**
     * Map User entity to AuthResponse (without tokens - tokens added separately).
     */
    @Mapping(target = "userId", expression = "java(user.getId().toString())")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "profile", expression = "java(toProfileData(user))")
    AuthResponse toAuthResponse(User user);

    /**
     * Map User entity to ProfileData.
     */
    @Mapping(target = "dateOfBirth", expression = "java(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null)")
    @Mapping(target = "address", expression = "java(toAddressResponse(user.getPrimaryAddress()))")
    ProfileData toProfileData(User user);

    /**
     * Update UserAddress from AddressRequest (for partial updates).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isPrimary", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromRequest(AddressRequest request, @MappingTarget UserAddress address);
}
