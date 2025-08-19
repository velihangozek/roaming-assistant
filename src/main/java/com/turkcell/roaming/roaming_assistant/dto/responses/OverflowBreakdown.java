package com.turkcell.roaming.roaming_assistant.dto.responses;

public record OverflowBreakdown(
        double over_gb, double over_min, double over_sms,
        double over_cost_data, double over_cost_voice, double over_cost_sms
) {}