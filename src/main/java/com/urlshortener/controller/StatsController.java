package com.urlshortener.controller;

import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "URL Statistics", description = "API for retrieving URL statistics")
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get URL statistics", 
               description = "Retrieves detailed statistics for a shortened URL")
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortCode) {
        UrlStatsResponse stats = statsService.getStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}/clicks")
    @Operation(summary = "Get click count", 
               description = "Retrieves the total number of clicks for a shortened URL")
    public ResponseEntity<Map<String, Long>> getClickCount(@PathVariable String shortCode) {
        long count = statsService.getStats(shortCode).getTotalClicks();
        return ResponseEntity.ok(Map.of("clicks", count));
    }

    @GetMapping("/{shortCode}/clicks/daily")
    @Operation(summary = "Get daily click statistics", 
               description = "Retrieves daily click statistics for a shortened URL")
    public ResponseEntity<Map<String, Long>> getDailyClicks(@PathVariable String shortCode) {
        Map<String, Long> dailyClicks = statsService.getStats(shortCode).getClicksByDay();
        return ResponseEntity.ok(dailyClicks);
    }

    @GetMapping("/{shortCode}/clicks/country")
    @Operation(summary = "Get clicks by country", 
               description = "Retrieves click statistics by country for a shortened URL")
    public ResponseEntity<Map<String, Long>> getClicksByCountry(@PathVariable String shortCode) {
        Map<String, Long> clicksByCountry = statsService.getStats(shortCode).getClicksByCountry();
        return ResponseEntity.ok(clicksByCountry);
    }

    @GetMapping("/{shortCode}/clicks/browser")
    @Operation(summary = "Get clicks by browser", 
               description = "Retrieves click statistics by browser for a shortened URL")
    public ResponseEntity<Map<String, Long>> getClicksByBrowser(@PathVariable String shortCode) {
        Map<String, Long> clicksByBrowser = statsService.getStats(shortCode).getClicksByBrowser();
        return ResponseEntity.ok(clicksByBrowser);
    }
}
