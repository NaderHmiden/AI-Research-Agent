package com.nader.aiagent.service;

import com.nader.aiagent.model.Platform;
import com.nader.aiagent.model.TrendAnalysis;
import com.nader.aiagent.model.TrendTopic;
import com.nader.aiagent.model.scrapedPost;
import com.nader.aiagent.repository.TrendAnalysisRepository;
import com.nader.aiagent.repository.TrendTopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmAnalysisService {
    private static String SYSTEM_PROMPT = """
            """;
    private final WebClient groqWebClient;
    private final TrendAnalysisRepository analysisRepository;
    private final TrendTopicRepository topicRepository;
    private final ObjectMapper objectMapper;

    @Value("${groq.model")
    private String model;


    public TrendAnalysis analyze(final List<scrapedPost> scrapedPosts) {
        final String userPrompt = buildPrompt(scrapedPosts );
        String rawResponse = null ;
        for (int i = 1; i<= 3 ; i++){
            try {
                final Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "max_tokens", 4000,
                        "system", SYSTEM_PROMPT,
                        "messages", List.of(Map.of("role", "user", "content", userPrompt)));
                rawResponse = this.groqWebClient.post()
                        .uri("/messages")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToFlux(String.class)
                        .blockLast();
                break;

            }catch(final Exception  e){
                log.warn("Anthropic attempt {}/3 failed: {}", i, e.getMessage());
                if(i < 3){
                    try{
                        Thread.sleep(2000);
                    }catch (final InterruptedException ie){
                        Thread.currentThread().interrupt();
                    }
                }

            }


        }
        if (rawResponse == null){
            log.error("All Groq Attempt failed to respond");
            rawResponse = "{}";
        }
        TrendAnalysis analysis = TrendAnalysis.builder()
                .rawAnalysis(rawResponse)
                .postsAnalysis(scrapedPosts.size())
                .build();
        analysis = this.analysisRepository.save(analysis);
        final List<TrendTopic> topics = parseTopics(rawResponse, analysis);
        this.topicRepository.saveAll(topics);
        log.info("Saved {} trend topics from analysis of {} posts.",topics.size(),scrapedPosts.size());

        return analysis;



    }
    private String buildPrompt(final  List<scrapedPost> posts){
        final StringBuilder prompt = new StringBuilder("");
        for(final scrapedPost post : posts){
            prompt.append(String.format("[%s] (score: %d,comments: %d) %s%n)",
                    post.getPlatform(),
                    post.getScore(),
                    post.getCommentCount(),
                    post.getTitle()
                     ));
            if(post.getContent() !=  null && !post.getContent().isBlank()) {
                final String snippet = post.getContent().length() > 200
                        ? post.getContent().substring(0,200) + "..."
                        : post.getContent();
                prompt.append(" > ").append(snippet).append("\n");
            }
            prompt.append("\n");

        }
        return prompt.toString();
    }
    private List<TrendTopic> parseTopics(final String rawResponse, final TrendAnalysis analysis){
        final List<TrendTopic> topics = new ArrayList<>();
        try{
            final JsonNode root = this.objectMapper.readTree(rawResponse);
            final JsonNode contentArray = root.path("content");
            if(!contentArray.isArray() || contentArray.isEmpty()){
                log.error("Unexpected groq response strucure: {}", rawResponse);
                return  topics;
            }
            String content = contentArray.get(0).path("text").asText("").strip();
            if(content.startsWith("```")){
                content = content.replaceFirst("^```[a-zA-Z]*\\n","").trim();
                content = content.replaceFirst("^```$\\n","").strip();
            }
            final JsonNode trendsArray = this.objectMapper.readTree(content);
            if(!trendsArray.isArray()) return topics;
            for (final JsonNode node : trendsArray){
                try{
                    final Platform pltform = parsePlatform(node.path("primaryPlatform").asText(""));
                    final List<String> relatedIds = new ArrayList<>();
                    final JsonNode relatedNode = node.path("relatedPostIds");
                    if(relatedNode.isArray()){
                        for (final JsonNode id : relatedNode){
                            relatedIds.add(id.asText());
                        }
                    }
                    final TrendTopic topic = TrendTopic.builder()
                            .topic(node.path("topic").asString(""))
                            .reasoning(node.path("reasoning").asString(""))
                            .category(node.path("category").asString((""))
                             .mentionCount(node.path("mentionCount").asInt(0))
                            .score(node.path("score").asDouble(0.0))
                            .primaryPlatform(pltform)
                                    .samplePostIds(String.join(",", relatedIds))
                                            .analysis(analysis)

                            .build();
                            topics.add(topic);
                }catch (final Exception e){
                    log.error("Failed to parse trend topic: {}", node, e);
                }
            }
         }catch(Exception e){
            log.error("Failed to parse trend topic: {}", rawResponse, e)
        }
        return topics;

    }
    private Platform parsePlatform(final String primaryPlatform){
        try{
            return Platform.valueOf(primaryPlatform.toUpperCase());

        }catch(final Exception e){
            return null;
        }
    }
}
