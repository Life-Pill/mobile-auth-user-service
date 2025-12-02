package com.lifepill.user_auth.repository;

import com.lifepill.user_auth.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserAddress entity operations.
 * Provides data access methods for user address management.
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    /**
     * Find all addresses for a user.
     *
     * @param userId the user ID
     * @return list of addresses for the user
     */
    List<UserAddress> findByUserId(UUID userId);

    /**
     * Find the primary address for a user.
     *
     * @param userId the user ID
     * @return an Optional containing the primary address if found
     */
    Optional<UserAddress> findByUserIdAndIsPrimaryTrue(UUID userId);

    /**
     * Delete all addresses for a user.
     *
     * @param userId the user ID
     */
    @Modifying
    @Query("DELETE FROM UserAddress ua WHERE ua.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Set all addresses for a user as non-primary.
     *
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isPrimary = false WHERE ua.user.id = :userId")
    void setAllNonPrimaryForUser(@Param("userId") UUID userId);
}
