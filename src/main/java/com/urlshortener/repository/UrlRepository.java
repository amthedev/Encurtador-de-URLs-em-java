package com.urlshortener.repository;

import com.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    
    @Query("SELECT u FROM UrlMapping u WHERE u.shortCode = :shortCode AND (u.expiresAt IS NULL OR u.expiresAt > :now) AND u.active = true")
    Optional<UrlMapping> findActiveByShortCode(@Param("shortCode") String shortCode, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(u) > 0 FROM UrlMapping u WHERE u.originalUrl = :url AND (u.expiresAt IS NULL OR u.expiresAt > CURRENT_TIMESTAMP) AND u.active = true")
    boolean existsActiveByOriginalUrl(@Param("url") String url);
    
    @Query("SELECT u FROM UrlMapping u WHERE u.originalUrl = :url AND u.active = true")
    Optional<UrlMapping> findByOriginalUrl(@Param("url") String url);
}
