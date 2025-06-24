package com.urlshortener.service;

import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.exception.UrlShortenerException;
import com.urlshortener.model.ClickStats;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.ClickStatsRepository;
import com.urlshortener.repository.UrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final ClickStatsRepository clickStatsRepository;
    private final UrlRepository urlRepository;

    @Autowired
    public StatsService(ClickStatsRepository clickStatsRepository, UrlRepository urlRepository) {
        this.clickStatsRepository = clickStatsRepository;
        this.urlRepository = urlRepository;
    }

    @Async
    @Transactional
    public void logClick(String shortCode, HttpServletRequest request) {
        UrlMapping urlMapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlShortenerException("URL not found"));

        ClickStats clickStats = new ClickStats();
        clickStats.setUrlMapping(urlMapping);
        clickStats.setIpAddress(getClientIpAddress(request));
        clickStats.setReferrer(request.getHeader("referer"));
        clickStats.setUserAgent(request.getHeader("User-Agent"));

        clickStatsRepository.save(clickStats);
    }

    @Cacheable(value = "stats", key = "#shortCode")
    public UrlStatsResponse getStats(String shortCode) {
        UrlMapping urlMapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlShortenerException("URL not found"));

        long totalClicks = clickStatsRepository.countByUrlMapping(urlMapping);
        
        // Get daily clicks for the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyClicks = clickStatsRepository.getDailyClickCounts(urlMapping);
        Map<String, Long> clicksByDay = dailyClicks.stream()
                .filter(entry -> ((java.sql.Date) entry[0]).toLocalDate().isAfter(thirtyDaysAgo.toLocalDate()))
                .collect(Collectors.toMap(
                        entry -> ((java.sql.Date) entry[0]).toLocalDate().toString(),
                        entry -> (Long) entry[1]
                ));

        // Get clicks by country
        List<Object[]> countryStats = clickStatsRepository.getClicksByCountry(urlMapping);
        Map<String, Long> clicksByCountry = countryStats.stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry[0],
                        entry -> (Long) entry[1]
                ));

        // Get clicks by browser
        List<Object[]> browserStats = clickStatsRepository.getClicksByBrowser(urlMapping);
        Map<String, Long> clicksByBrowser = browserStats.stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry[0],
                        entry -> (Long) entry[1]
                ));

        UrlStatsResponse response = new UrlStatsResponse(
                urlMapping.getOriginalUrl(),
                baseUrl + "/" + urlMapping.getShortCode(),
                urlMapping.getShortCode(),
                totalClicks,
                urlMapping.getCreatedAt(),
                urlMapping.getExpiresAt()
        );

        response.setClicksByDay(clicksByDay);
        response.setClicksByCountry(clicksByCountry);
        response.setClicksByBrowser(clicksByBrowser);

        return response;
    }

    public long getClicksInLast24Hours(UrlMapping urlMapping) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return clickStatsRepository.countClicksInDateRange(
                urlMapping, 
                twentyFourHoursAgo, 
                LocalDateTime.now()
        );
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}
