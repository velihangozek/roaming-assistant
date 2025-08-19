package com.turkcell.roaming.roaming_assistant.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser {
    @Id private Long userId;
    private String name;
    private String homePlan; // Faturalı / Faturasız
}