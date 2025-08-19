package com.turkcell.roaming.roaming_assistant.model.entity;

import jakarta.persistence.*; import lombok.*;

@Entity @Table(name="usage_profile")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UsageProfile {
    @Id private Long userId;
    @OneToOne @MapsId @JoinColumn(name="user_id") private AppUser user;
    private int avgDailyMb;
    private int avgDailyMin;
    private int avgDailySms;
}