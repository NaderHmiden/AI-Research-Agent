package com.nader.aiagent.service;

import com.nader.aiagent.model.Platform;
import com.nader.aiagent.model.scrapedPost;
import com.nader.aiagent.repository.ScrapedPostRepository;
import com.nader.aiagent.scraper.PlatformScraper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class Orchestration {
    private final ScrapedPostRepository scrapedPostRepository;
    private List<PlatformScraper> scrapers;
    public Map<Platform,Integer> scrapeAll(){
        final Map<Platform, Integer> results = new EnumMap<>(Platform.class);
        for(final PlatformScraper scraper : scrapers){
            try {
                final List<scrapedPost> scrapedPosts = scraper.scrape();
                final List<scrapedPost> saved = this.scrapedPostRepository.saveAll(scrapedPosts);
                results.put(scraper.getPlatform(),saved.size());
            }catch (final Exception e) {
                log.error("Failed to scrape {}",scraper.getPlatform(),e.getMessage());
                results.put(scraper.getPlatform(),0);
            }
        }
        return results;
    }
    public List<scrapedPost> scrapePlatform(final Platform platform){
        return   scrapers.stream()
                .filter(p -> p.getPlatform() == platform)
                .findFirst()
                .map(PlatformScraper::scrape)
                .map(scrapedPostRepository::saveAll)
                .orElse(List.of());
    }

}
