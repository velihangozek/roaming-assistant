package com.turkcell.roaming.roaming_assistant.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="countries")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Country {
    @Id @Column(length=3) private String countryCode;
    private String countryName;
    private String region;
}