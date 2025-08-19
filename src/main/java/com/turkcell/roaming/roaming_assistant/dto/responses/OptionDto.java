package com.turkcell.roaming.roaming_assistant.dto.responses;

public record OptionDto(
        String kind, Long pack_id, String pack_name, int n_packs,
        boolean coverage_hit, boolean validity_ok,
        OverflowBreakdown overflow_breakdown,
        double total_cost, String currency, Double total_cost_try
) {}