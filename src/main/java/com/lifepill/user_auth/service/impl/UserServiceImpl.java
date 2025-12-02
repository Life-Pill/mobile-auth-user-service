package com.lifepill.user_auth.service.impl;

import com.lifepill.user_auth.dto.request.UpdateProfileRequest;
import com.lifepill.user_auth.dto.response.UserProfileResponse;
import com.lifepill.user_auth.entity.User;
import com.lifepill.user_auth.entity.UserAddress;
import com.lifepill.user_auth.exception.UserNotFoundException;
import com.lifepill.user_auth.mapper.UserMapper;
import com.lifepill.user_auth.repository.UserAddressRepository;
import com.lifepill.user_auth.repository.UserRepository;
import com.lifepill.user_auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementation of UserService for user profile operations.
 * Handles profile retrieval and updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        log.info("Fetching profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        return userMapper.toUserProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        // Update basic fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }

        // Update address if provided
        if (request.getAddress() != null) {
            updateUserAddress(user, request);
        }

        User savedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", userId);

        return userMapper.toUserProfileResponse(savedUser);
    }

    private void updateUserAddress(User user, UpdateProfileRequest request) {
        UserAddress existingAddress = user.getPrimaryAddress();

        if (existingAddress != null) {
            // Update existing address
            if (request.getAddress().getStreet() != null) {
                existingAddress.setStreet(request.getAddress().getStreet());
            }
            if (request.getAddress().getCity() != null) {
                existingAddress.setCity(request.getAddress().getCity());
            }
            if (request.getAddress().getState() != null) {
                existingAddress.setState(request.getAddress().getState());
            }
            if (request.getAddress().getZipCode() != null) {
                existingAddress.setZipCode(request.getAddress().getZipCode());
            }
            if (request.getAddress().getCountry() != null) {
                existingAddress.setCountry(request.getAddress().getCountry());
            }
        } else {
            // Create new address
            UserAddress newAddress = userMapper.toUserAddress(request.getAddress());
            user.addAddress(newAddress);
        }
    }
}
