package com.medpoint.repository;

import com.medpoint.entity.DeveloperOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface DeveloperOtpRepository extends JpaRepository<DeveloperOtp, Long> {

    Optional<DeveloperOtp> findTopByEmailAndUsedFalseOrderByExpiresAtDesc(String email);

    @Modifying
    @Transactional
    @Query("UPDATE DeveloperOtp o SET o.used = true WHERE o.email = :email AND o.used = false")
    void invalidateAllForEmail(String email);
}
