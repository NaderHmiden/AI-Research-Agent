package com.nader.aiagent.repository;

import com.nader.aiagent.model.TrendAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrendAnalysisRepository extends JpaRepository<TrendAnalysis,Long> {
    Optional<TrendAnalysis> findTopByOrderByAnalyzedAtDesc();
}
