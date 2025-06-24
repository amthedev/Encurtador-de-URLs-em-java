package com.urlshortener.service;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.exception.UrlShortenerException;
import com.urlshortener.model.ClickStats;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.ClickStatsRepository;
import com.urlshortener.repository.UrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlShortenerService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-url-length}")
    private int shortUrlLength;

    @Value("${app.short-url-chars}")
    private String shortUrlChars;

    private final UrlRepository urlRepository;
    private final ClickStatsRepository clickStatsRepository;

    @Autowired
    public UrlShortenerService(UrlRepository urlRepository, ClickStatsRepository clickStatsRepository) {
        this.urlRepository = urlRepository;
        this.clickStatsRepository = clickStatsRepository;
    }

    @Transactional
    public String shortenUrl(ShortenUrlRequest request) {
        String originalUrl = normalizeUrl(request.getOriginalUrl());
        
        // Check if this URL already exists and is still valid
        if (urlRepository.existsActiveByOriginalUrl(originalUrl)) {
            Optional<UrlMapping> existingMapping = urlRepository.findByOriginalUrl(originalUrl);
            if (existingMapping.isPresent()) {
                return baseUrl + "/" + existingMapping.get().getShortCode();
            }
        }

        String shortCode = generateUniqueShortCode();
        if (request.getCustomAlias() != null && !request.getCustomAlias().isEmpty()) {
            if (urlRepository.existsByShortCode(request.getCustomAlias())) {
                throw new UrlShortenerException("Custom alias is already in use");
            }
            shortCode = request.getCustomAlias();
        }

        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortCode(shortCode);
        urlMapping.setExpiresAt(request.getExpiresAt());
        urlMapping.setActive(true);

        urlRepository.save(urlMapping);

        return baseUrl + "/" + shortCode;
    }

    @Cacheable(value = "urls", key = "#shortCode", unless = "#result == null")
    public String getOriginalUrl(String shortCode) {
        return urlRepository.findActiveByShortCode(shortCode, LocalDateTime.now())
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new UrlShortenerException("URL not found or has expired"));
    }

    @CacheEvict(value = "urls", key = "#shortCode")
    public void deactivateUrl(String shortCode) {
        UrlMapping urlMapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlShortenerException("URL not found"));
        
        urlMapping.setActive(false);
        urlRepository.save(urlMapping);
    }

    private String generateUniqueShortCode() {
        String shortCode;
        do {
            shortCode = RandomStringUtils.random(shortUrlLength, shortUrlChars);
        } while (urlRepository.existsByShortCode(shortCode));
        
        return shortCode;
    }

    @Async
    public void logClick(String shortCode, HttpServletRequest request) {
        UrlMapping urlMapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlShortenerException("URL not found"));
        
        ClickStats clickStats = new ClickStats();
        clickStats.setUrlMapping(urlMapping);
        clickStats.setIpAddress(request.getRemoteAddr());
        clickStats.setReferrer(request.getHeader("Referer"));
        clickStats.setUserAgent(request.getHeader("User-Agent"));
        clickStats.setClickedAt(LocalDateTime.now());
        
        clickStatsRepository.save(clickStats);
    }

    private String normalizeUrl(String url) {
        try {
            URL urlObj = new URL(url);
            String protocol = urlObj.getProtocol().toLowerCase();
            String host = urlObj.getHost().toLowerCase();
            int port = urlObj.getPort();
            String path = urlObj.getPath();
            String query = urlObj.getQuery();
            
            StringBuilder normalizedUrl = new StringBuilder();
            normalizedUrl.append(protocol).append("://").append(host);
            
            if (port != -1 && !((protocol.equals("http") && port == 80) || 
                               (protocol.equals("https") && port == 443))) {
                normalizedUrl.append(":").append(port);
            }
            
            if (path != null && !path.isEmpty()) {
                if (!path.startsWith("/")) {
                    normalizedUrl.append("/");
                }
                normalizedUrl.append(path);
            }
            
            if (query != null && !query.isEmpty()) {
                normalizedUrl.append("?").append(query);
            }
            
            return normalizedUrl.toString();
        } catch (MalformedURLException e) {
            throw new UrlShortenerException("Invalid URL format");
        }
    }
}
