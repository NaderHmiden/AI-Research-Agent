package com.nader.aiagent.scraper;

import com.nader.aiagent.config.ProxyConfig;
import com.nader.aiagent.model.Platform;
import com.nader.aiagent.model.scrapedPost;
import com.nader.aiagent.repository.ScrapedPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
@Component
@Slf4j
public class RedditScraper extends AbstractScraper implements PlatformScraper{
    private final ScrapedPostRepository scrapedPostRepository;
    private final ObjectMapper objectMapper;
    public RedditScraper(final ProxyConfig proxyConfig,
                         final ScrapedPostRepository scrapedPostRepository,
                         final ObjectMapper objectMapper){
        super(proxyConfig);
        this.scrapedPostRepository = scrapedPostRepository;
        this.objectMapper = objectMapper;
    }
    @Value("${scraping.reddit.subreddits}")
    private List<String> subreddits;
    @Value("${scraping.reddit.posts-per-subreddit}")
    private int postsPerSubreddit;
    @Override
    public Platform getPlatform() {
         return Platform.REDDIT;
    }

    @Override
    public List<scrapedPost> scrape() {
       final List<scrapedPost> posts = new ArrayList<>();
       log.info("Reddit scrapper started");
       log.info("Reddit scraper using subreddit: {}",this.subreddits);
       for(final String subreddit : this.subreddits ){
           try {
               final  String url = "https://www.reddit.com/r/" + subreddit + "/hoy.json?limit=" + this.postsPerSubreddit;
               final String json = fetch(url);
               final String proxyIp = detectProxyId();
               final JsonNode root = this.objectMapper.readTree(json);
               final JsonNode children = root.path("data")
                       .path("children");
               for(final JsonNode child : children) {
                   final JsonNode data  = child.path("data");
                   final String externalId = data.path("id").asString("");
                   if(externalId.isBlank()){
                       continue;
                   }
                   if(this.scrapedPostRepository.existByPlatformAndExternalId(getPlatform(),externalId)){
                       continue;
                   }
                   final String title  = data.path("title").asString("");
                   if(title.isBlank()){
                       continue;
                   }
                   final  String selfText = data.path("selfText").asString("").trim();
                   final String content = selfText.isBlank()
                           ? title.substring(0,Math.min(title.length(), 500))
                           : selfText;
                   final long postedAtEpoch = (long) data.path("created_utc")
                           .asDouble();
                   final LocalDateTime postedAt = data.has("created_utc")
                           ? LocalDateTime.ofInstant(
                           Instant.ofEpochSecond(postedAtEpoch),
                           ZoneId.systemDefault())
                           : null;

                   final String redditurl = data.path("url").asString("");
                   final  String author = data.path("author").asString("");
                   final int score = data.path("score").asInt(0);
                   final int commentCount = data.path("num comments").asInt(0);
                   final String subredditName = data.path("subreddit").asString(subreddit);
                   final scrapedPost scrapedpost = scrapedPost.builder()
                           .platform(getPlatform())
                           .externalId(externalId)
                           .title(title)
                           .content(content)
                           .proxyIpUsed(proxyIp)
                           .url(url)
                           .author(author)
                           .score(score)
                           .commentCount(commentCount)
                           .postedAt(postedAt)
                           .subReddit(subredditName)
                           .build();
                   posts.add(scrapedpost);
                   log.info("Reddit r/{} scraped: {} new posts",subreddit, posts.size());

                   Thread.sleep(500);


               }

           }catch (final InterruptedException e){
            Thread.currentThread().interrupt();
            break;
           }
           catch (IOException e) {
               log.error("Failed to scrape Reddit r/{}",subreddit, e.getMessage());
           }
       }

        return posts;
    }
}
