package com.turkcell.roaming.roaming_assistant.dto.requests;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CheckoutRequest(
        @NotNull Long userId,
        @NotNull Selection selection
) {}