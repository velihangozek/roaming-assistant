package com.turkcell.roaming.roaming_assistant.model.entity;

import jakarta.persistence.*; import lombok.*;

@Entity @Table(name="roaming_rates")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RoamingRate {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="country_code") private Country country;
    private double dataPerMb;
    private double voicePerMin;
    private double smsPerMsg;
    private String currency; // EUR, USD
}