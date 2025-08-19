package com.turkcell.roaming.roaming_assistant.dto.responses;

public record TopOptionDto(
        String label, double total_cost, String currency, Double total_cost_try,
        String explanation, String details
) {}