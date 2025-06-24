package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShortenUrlRequest {
    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", 
             message = "Invalid URL format. Must start with http:// or https://")
    private String originalUrl;
    
    private LocalDateTime expiresAt;
    
    private String customAlias;
}
