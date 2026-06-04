package com.nader.aiagent.scraper;

import com.nader.aiagent.model.Platform;
import com.nader.aiagent.model.scrapedPost;

import java.util.List;

public interface PlatformScraper {
    Platform getPlatform();
    List<scrapedPost> scrape();
}
