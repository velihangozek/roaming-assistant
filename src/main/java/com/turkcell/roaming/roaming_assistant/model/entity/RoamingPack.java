package com.turkcell.roaming.roaming_assistant.model.entity;

import com.turkcell.roaming.roaming_assistant.model.CoverageType;
import jakarta.persistence.*; import lombok.*;

@Entity @Table(name="roaming_packs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RoamingPack {
    @Id private Long packId;
    private String name;
    private String coverage;               // "Europe", "US", "Global", "AE"...
    @Enumerated(EnumType.STRING) private CoverageType coverageType;
    private double dataGb;
    private int voiceMin;
    private int sms;
    private double price;
    private int validityDays;
    private String currency;               // EUR / USD
}