package com.turkcell.roaming.roaming_assistant.business.abstracts;

import com.turkcell.roaming.roaming_assistant.dto.requests.RecommendationRequest;
import com.turkcell.roaming.roaming_assistant.dto.responses.RationaleDto;
public interface RecommendationService { RationaleDto recommend(RecommendationRequest req); }