package com.turkcell.roaming.roaming_assistant.dto.requests;

public record RecommendationRequest(
        Long user_id, java.util.List<TripInput> trips, SimulateRequest.Profile profile
) {}