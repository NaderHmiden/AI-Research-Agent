package com.nader.aiagent.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tren_analysis"
       )
public class TrendAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawAnalysis;
    @Enumerated(EnumType.STRING)
    private Platform platform;
    private int postsAnalysis;
    private LocalDateTime analyzedAt;
    @PrePersist
    public void prePersist(){
        this.analyzedAt = LocalDateTime.now();
    }
}
