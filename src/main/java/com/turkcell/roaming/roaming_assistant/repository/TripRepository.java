package com.turkcell.roaming.roaming_assistant.repository;

import com.turkcell.roaming.roaming_assistant.model.entity.Trip;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @Query("select t from Trip t where t.user.userId = :uid order by t.startDate")
    List<Trip> findByUserId(@Param("uid") Long userId);
}