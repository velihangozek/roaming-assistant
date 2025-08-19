package com.turkcell.roaming.roaming_assistant.dto.responses;

import java.util.List;
public record SimulateResponse(SummaryDto summary, List<OptionDto> options) {}