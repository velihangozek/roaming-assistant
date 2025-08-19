package com.turkcell.roaming.roaming_assistant.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Selection(
        @NotBlank
        String kind,                               // "pack" | "payg"

        @JsonProperty("pack_id")
        Long packId,                               // kind=pack ise zorunlu

        @JsonProperty("n_packs")
        @Positive
        Integer nPacks                             // kind=pack ise zorunlu
) {}