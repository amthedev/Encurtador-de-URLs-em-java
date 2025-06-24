package com.urlshortener.controller;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@Tag(name = "URL Shortener", description = "API for shortening URLs")
public class UrlController {

    private final UrlShortenerService urlShortenerService;

    @Autowired
    public UrlController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @PostMapping("/shorten")
    @Operation(summary = "Shorten a URL", 
               description = "Creates a shortened version of the provided URL")
    public ResponseEntity<Map<String, String>> shortenUrl(
            @Valid @RequestBody ShortenUrlRequest request,
            HttpServletRequest httpRequest) {
        
        String shortUrl = urlShortenerService.shortenUrl(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("shortUrl", shortUrl);
        response.put("originalUrl", request.getOriginalUrl());
        
        String statsUrl = httpRequest.getRequestURL().toString()
                .replace("/shorten", "/" + shortUrl.substring(shortUrl.lastIndexOf('/') + 1) + "/stats");
        response.put("statsUrl", statsUrl);
        
        return ResponseEntity.created(URI.create(shortUrl)).body(response);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to original URL", 
               description = "Redirects the short URL to the original URL")
    public RedirectView redirectToOriginalUrl(
            @PathVariable String shortCode,
            HttpServletRequest request) {
        
        String originalUrl = urlShortenerService.getOriginalUrl(shortCode);
        
        // Log the click asynchronously
        urlShortenerService.logClick(shortCode, request);
        
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(originalUrl);
        redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        return redirectView;
    }

    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Deactivate a short URL", 
               description = "Deactivates a short URL so it can no longer be used")
    public ResponseEntity<Void> deactivateUrl(@PathVariable String shortCode) {
        urlShortenerService.deactivateUrl(shortCode);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{shortCode}/qrcode")
    @Operation(summary = "Get QR code for short URL", 
               description = "Generates a QR code for the short URL")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String shortCode) {
        // This would be implemented to generate a QR code for the short URL
        // For now, we'll return a placeholder response
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
