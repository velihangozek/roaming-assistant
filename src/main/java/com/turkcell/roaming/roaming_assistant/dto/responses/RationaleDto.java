package com.turkcell.roaming.roaming_assistant.dto.responses;

import java.util.List;
public record RationaleDto(List<TopOptionDto> top3, String rationale) {}