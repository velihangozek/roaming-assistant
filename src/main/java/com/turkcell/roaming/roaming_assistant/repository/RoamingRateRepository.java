package com.turkcell.roaming.roaming_assistant.repository;

import com.turkcell.roaming.roaming_assistant.model.entity.Country;
import com.turkcell.roaming.roaming_assistant.model.entity.RoamingRate;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface RoamingRateRepository extends JpaRepository<RoamingRate, Long> {
    @Query("select r from RoamingRate r where r.country.countryCode = :cc")
    Optional<RoamingRate> findByCountryCode(@Param("cc") String countryCode);

    Optional<RoamingRate> findByCountryAndCurrency(Country country, String currency);

    boolean existsByCountryAndCurrency(Country country, String currency);

    @Query("""
    select r from RoamingRate r 
     where r.country.region = :region
  """)
    List<RoamingRate> findAllByRegion(@Param("region") String region);
}