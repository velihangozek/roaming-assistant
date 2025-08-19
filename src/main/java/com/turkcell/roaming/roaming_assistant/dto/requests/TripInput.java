package com.turkcell.roaming.roaming_assistant.dto.requests;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record TripInput(
        @NotBlank String country_code,
        @NotNull LocalDate start_date,
        @NotNull LocalDate end_date
) {}