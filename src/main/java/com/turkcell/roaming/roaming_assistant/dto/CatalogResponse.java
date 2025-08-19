package com.turkcell.roaming.roaming_assistant.dto;

import com.turkcell.roaming.roaming_assistant.model.entity.*;
import java.util.List;

public record CatalogResponse(List<Country> countries, List<RoamingRate> rates, List<RoamingPack> packs) {}