package com.turkcell.roaming.roaming_assistant.dto.requests;

import jakarta.validation.constraints.*;
import java.util.List;

public record SimulateRequest(
        @NotNull Long user_id,
        @NotEmpty List<TripInput> trips,
        @NotNull Profile profile
){
    public record Profile(@Positive int avg_daily_mb, @Positive int avg_daily_min, @Positive int avg_daily_sms){}
}