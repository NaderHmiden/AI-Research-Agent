package com.nader.aiagent.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.sql.ast.tree.expression.JsonTableColumnDefinition;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tren_topic"
)
public class TrendTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 512)
   private String topic;
    @Column(columnDefinition = "TEXT")
   private String summary;
    @Column(columnDefinition = "TEXT")
    private String reasoning;
    private String category;
    private int mentionCount;
    private double score;
    @Enumerated(EnumType.STRING)
    private Platform primaryPlatform;
    @Column(length= 2048)
    private String simplePostIds;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private TrendAnalysis analysis;
    private LocalDateTime detectedAt;
    @PrePersist
    public void prePersist(){
        this.detectedAt= LocalDateTime.now();
    }




}
