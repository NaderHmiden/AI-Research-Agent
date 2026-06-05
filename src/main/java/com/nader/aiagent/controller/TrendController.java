package com.nader.aiagent.controller;

import com.nader.aiagent.model.TrendTopic;
import com.nader.aiagent.service.TrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trends")
@RequiredArgsConstructor
public class TrendController {
    private final TrendService trendService;

    @GetMapping
    public ResponseEntity<List<TrendTopic>> getTopTrendds() {
        return ResponseEntity.ok(this.trendService.getTopTrends());

    }
    @GetMapping("/latest")
    public ResponseEntity<List<TrendTopic>> getLatestTrendds() {
        return ResponseEntity.ok(this.trendService.getLatestTrends());

    }
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TrendTopic>> getTrendByCategory(
            @PathVariable final String category
    ) {
        return ResponseEntity.ok(this.trendService.getTrendByCategory(category));

    }
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<TrendTopic>> getTrendByPlatform(
            @PathVariable final String platform
    ) {
        return ResponseEntity.ok(this.trendService.getTrendByPlatform(platform));

    }
    @GetMapping("/stats")
    public ResponseEntity<Map<String,Object>> getDashboardStatd(){
        return ResponseEntity.ok(this.trendService.getDashboardStats());
    }

}
