package com.turkcell.roaming.roaming_assistant.controller;

import com.turkcell.roaming.roaming_assistant.business.abstracts.SimulationService;
import com.turkcell.roaming.roaming_assistant.dto.requests.SimulateRequest;
import com.turkcell.roaming.roaming_assistant.dto.responses.SimulateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/simulate") @RequiredArgsConstructor
public class SimulationController {
    private final SimulationService service;
    @PostMapping public SimulateResponse post(@Valid @RequestBody SimulateRequest req){ return service.simulate(req); }
}