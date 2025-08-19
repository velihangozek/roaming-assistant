package com.turkcell.roaming.roaming_assistant.business.concretes;

import com.turkcell.roaming.roaming_assistant.business.abstracts.CatalogService;
import com.turkcell.roaming.roaming_assistant.dto.CatalogResponse;
import com.turkcell.roaming.roaming_assistant.repository.*;
import org.springframework.stereotype.Service; import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class CatalogManager implements CatalogService {
    private final CountryRepository countryRepo; private final RoamingRateRepository rateRepo; private final RoamingPackRepository packRepo;
    @Override public CatalogResponse getCatalog() {
        return new CatalogResponse(countryRepo.findAll(), rateRepo.findAll(), packRepo.findAll());
    }
}