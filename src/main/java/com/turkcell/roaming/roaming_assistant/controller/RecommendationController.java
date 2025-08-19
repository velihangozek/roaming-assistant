package com.turkcell.roaming.roaming_assistant.controller;

import com.turkcell.roaming.roaming_assistant.business.abstracts.RecommendationService;
import com.turkcell.roaming.roaming_assistant.dto.requests.RecommendationRequest;
import com.turkcell.roaming.roaming_assistant.dto.responses.RationaleDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/recommendation") @RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService service;
    @PostMapping public RationaleDto post(@Valid @RequestBody RecommendationRequest req){ return service.recommend(req); }
}