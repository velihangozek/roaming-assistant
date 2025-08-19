package com.turkcell.roaming.roaming_assistant.controller;

import com.turkcell.roaming.roaming_assistant.business.abstracts.CheckoutService;
import com.turkcell.roaming.roaming_assistant.dto.requests.CheckoutRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService service;

    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> post(@Valid @RequestBody CheckoutRequest req) {
        String order = service.checkout(req.userId(), req.selection());
        return Map.of("status", "ok", "order_id", order);
    }
}