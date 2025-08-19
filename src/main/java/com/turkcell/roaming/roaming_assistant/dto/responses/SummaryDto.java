package com.turkcell.roaming.roaming_assistant.dto.responses;

public record SummaryDto(
        int total_days, double total_need_gb, int total_need_min, int total_need_sms
) {}