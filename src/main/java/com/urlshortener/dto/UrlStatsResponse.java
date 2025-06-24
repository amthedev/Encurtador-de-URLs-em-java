package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class UrlStatsResponse {
    private String originalUrl;
    private String shortUrl;
    private String shortCode;
    private long totalClicks;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private Map<String, Long> clicksByDay;
    private Map<String, Long> clicksByCountry;
    private Map<String, Long> clicksByBrowser;
    
    public UrlStatsResponse(String originalUrl, String shortUrl, String shortCode, 
                           long totalClicks, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.shortCode = shortCode;
        this.totalClicks = totalClicks;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
