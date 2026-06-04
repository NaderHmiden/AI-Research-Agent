package com.nader.aiagent.service;

import com.nader.aiagent.model.Platform;
import com.nader.aiagent.model.TrendTopic;
import com.nader.aiagent.repository.ScrapedPostRepository;
import com.nader.aiagent.repository.TrendAnalysisRepository;
import com.nader.aiagent.repository.TrendTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrendService
{
    private final TrendTopicRepository trendTopicRepository;
    private  final ScrapedPostRepository scrapedPostRepository;
    private final TrendAnalysisRepository trendAnalysisRepository;
    public List<TrendTopic> getLatestTrends() {
        final LocalDateTime since = LocalDateTime.now().minusHours(24);
        return this.trendTopicRepository.findByDetectedAtAfterOrderByTrendScoreDesc(since);
    }

    public List<TrendTopic> getTopTrends() {
        return this.trendTopicRepository.findTop20ByOrderByTrendScoreDesc();
    }

    public List<TrendTopic> getTrendByCategory(final String category) {
        return this.trendTopicRepository.findByCategoryOrderByTrendScoreDesc(category);
    }

    public List<TrendTopic> getTrendByPlatform(final  String platform) {
        return this.trendTopicRepository.findByPrimaryPlatformOrderByTrendScoreDesc(platform);

    }
    public Map<String,Object> getDashboardStats(){
        return Map.of(
                "totalPosts", this.scrapedPostRepository.count(),
                "redditPosts",this.scrapedPostRepository.countByPlatform(Platform.REDDIT),
                "hnPosts",this.scrapedPostRepository.countByPlatform(Platform.HACKERNEWS),
                "phPosts",this.scrapedPostRepository.countByPlatform(Platform.PRODUCTHUNT),
                "totalTrends",this.trendTopicRepository.count(),
                "lastAnalysis",this.trendAnalysisRepository.findTopByOrderByAnalyzedAtDesc()

        );
    }





}
