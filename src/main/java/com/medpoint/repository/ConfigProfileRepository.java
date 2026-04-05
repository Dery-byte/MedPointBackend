package com.medpoint.repository;

import com.medpoint.entity.ConfigProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfigProfileRepository extends JpaRepository<ConfigProfile, Long> {
    List<ConfigProfile> findAllByOrderBySavedAtDesc();
}
