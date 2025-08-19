package com.turkcell.roaming.roaming_assistant.repository;

import com.turkcell.roaming.roaming_assistant.model.entity.AppUser;
import com.turkcell.roaming.roaming_assistant.model.entity.UsageProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsageProfileRepository extends JpaRepository<UsageProfile, Long> {

    Optional<UsageProfile> findByUser(AppUser user);

    boolean existsByUser(AppUser user);

}