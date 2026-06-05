package com.nader.aiagent.scheduler;

import com.nader.aiagent.model.Platform;
import com.nader.aiagent.model.scrapedPost;
import com.nader.aiagent.repository.ScrapedPostRepository;
import com.nader.aiagent.service.Orchestration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResearchSchedular {
    private final Orchestration orchestration;
    private final ScrapedPostRepository scrapedPostRepository;
    private final LlmAnalysisService analysisService;
    @Scheduled(cron = "${scraping.cron")
    public void runResearchCycle() {
        log.info("=== Research cycle started ===");
        final Map<Platform, Integer> results = this.orchestration.scrapeAll();
        log.info("Scraping complete: {}", results);
        final LocalDateTime since = LocalDateTime.now().minusHours(6);
        final List<scrapedPost> recentPost = this.scrapedPostRepository.finAllScrapedAtAfterOrderByScoreDesc(since);
        if(!recentPost.isEmpty()){
            this.analysisService.analyze(recentPost);
            log.info(" LLM analysis completed for {} posts", recentPost.size());
        }
        log.info("=== Research cycle completed===");
    }

}
