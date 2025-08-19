package com.turkcell.roaming.roaming_assistant.controller;

import com.turkcell.roaming.roaming_assistant.business.abstracts.CatalogService;
import com.turkcell.roaming.roaming_assistant.dto.CatalogResponse;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/catalog") @RequiredArgsConstructor
public class CatalogController {
    private final CatalogService srv;
    @GetMapping public CatalogResponse get(){ return srv.getCatalog(); }
}