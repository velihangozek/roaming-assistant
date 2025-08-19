package com.turkcell.roaming.roaming_assistant.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name="trips")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Trip {
    @Id private Long tripId;
    @ManyToOne(optional=false) @JoinColumn(name="user_id") private AppUser user;
    @ManyToOne(optional=false) @JoinColumn(name="country_code") private Country country;
    private LocalDate startDate;
    private LocalDate endDate;
}