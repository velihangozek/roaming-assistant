package com.turkcell.roaming.roaming_assistant.exception;

import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> nf(NotFoundException ex){ return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage())); }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> br(BadRequestException ex){ return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> ex(Exception ex){ return ResponseEntity.status(500).body(Map.of("error", ex.getMessage())); }
}