package com.turkcell.roaming.roaming_assistant.repository;

import com.turkcell.roaming.roaming_assistant.model.CoverageType;
import com.turkcell.roaming.roaming_assistant.model.entity.RoamingPack;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RoamingPackRepository extends JpaRepository<RoamingPack, Long> {
    @Query("""
              select p from RoamingPack p 
              where (p.coverageType = com.turkcell.roaming.roaming_assistant.model.CoverageType.REGION and p.coverage = :region)
                 or (p.coverageType = com.turkcell.roaming.roaming_assistant.model.CoverageType.COUNTRY and p.coverage = :country)
                 or (p.coverageType = com.turkcell.roaming.roaming_assistant.model.CoverageType.REGION and p.coverage = 'Global')
            """)
    List<RoamingPack> findPacksCovering(@Param("region") String region, @Param("country") String countryCode);

    @Query("select p from RoamingPack p where p.coverageType = :ct")
    List<RoamingPack> findByCoverageType(@Param("ct") CoverageType ct);
}