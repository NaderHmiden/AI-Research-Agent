package com.nader.aiagent.repository;

import com.nader.aiagent.model.TrendTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrendTopicRepository extends JpaRepository<TrendTopic,Long> {
    List<TrendTopic> findByDetectedAtAfterOrderByTrendScoreDesc(LocalDateTime since);

    List<TrendTopic> findTop20ByOrderByTrendScoreDesc();

    List<TrendTopic> findByCategoryOrderByTrendScoreDesc(String category);

    List<TrendTopic> findByPrimaryPlatformOrderByTrendScoreDesc(String platform);
}
