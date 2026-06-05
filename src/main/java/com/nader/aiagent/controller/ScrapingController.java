package com.nader.aiagent.controller;

import com.nader.aiagent.model.Platform;
import com.nader.aiagent.model.TrendAnalysis;
import com.nader.aiagent.model.TrendTopic;
import com.nader.aiagent.model.scrapedPost;
import com.nader.aiagent.repository.ScrapedPostRepository;
import com.nader.aiagent.service.Orchestration;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(":api/scrape")
@RequiredArgsConstructor
public class ScrapingController {
    private final Orchestration orchestration;
    private final LlmAnalysisService llmAnalysisService;
    private final ScrapedPostRepository scrapedPostRepository;
    @PatchMapping("/run")
    public ResponseEntity<Map<String, Object>> triggerFullCycle(){
        final Map<Platform, Integer> scrapedResults = this.orchestration.scrapeAll();
        final LocalDateTime since = LocalDateTime.now().minusHours(6);
        final List<scrapedPost> posts = this.scrapedPostRepository.finAllScrapedAtAfterOrderByScoreDesc(since);
        TrendAnalysis trendAnalysis = null;
        if(!posts.isEmpty()){
            trendAnalysis = this.llmAnalysisService.analyze(posts);
        }
        return ResponseEntity.ok(
                Map.of(
                        "scrapedResults", scrapedResults,
                        "postAnalyzed", posts.size(),
                        "analysisId", trendAnalysis != null ? trendAnalysis.getId() : "none"
                )

        );

    }
    @PostMapping("/platform/{platform}")
    public ResponseEntity<List<scrapedPost>> scrapedPlatform(
      @PathVariable final Platform platfom
    ){
        return  ResponseEntity.ok(this.orchestration.scrapePlatform(platfom));
    }
    @GetMapping("/posts")
    public ResponseEntity<List<scrapedPost>>getRecentPosts(
            @RequestParam(required = false)
            final Platform platform

    ){
        if(platform != null) {
            return ResponseEntity.ok(this.scrapedPostRepository.findByPlatformOrderByScrapedAtDesc(platform);
        }

    }

}
