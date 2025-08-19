package com.turkcell.roaming.roaming_assistant.repository;

import com.turkcell.roaming.roaming_assistant.model.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {

}