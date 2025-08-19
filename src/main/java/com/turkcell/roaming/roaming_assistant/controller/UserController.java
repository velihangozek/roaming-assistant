package com.turkcell.roaming.roaming_assistant.controller;

import com.turkcell.roaming.roaming_assistant.exception.NotFoundException;
import com.turkcell.roaming.roaming_assistant.model.entity.AppUser;
import com.turkcell.roaming.roaming_assistant.repository.UserRepository;
import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepo;
    @GetMapping("/{id}") public AppUser get(@PathVariable Long id){
        return userRepo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }
}