package com.urlshortener.repository;

import com.urlshortener.model.ClickStats;
import com.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ClickStatsRepository extends JpaRepository<ClickStats, Long> {
    long countByUrlMapping(UrlMapping urlMapping);
    
    @Query("SELECT COUNT(c) FROM ClickStats c WHERE c.urlMapping = :urlMapping AND c.clickedAt >= :startDate AND c.clickedAt < :endDate")
    long countClicksInDateRange(
            @Param("urlMapping") UrlMapping urlMapping,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT CAST(c.clickedAt AS date) AS date, COUNT(c) AS count " +
           "FROM ClickStats c WHERE c.urlMapping = :urlMapping " +
           "GROUP BY CAST(c.clickedAt AS date) " +
           "ORDER BY date DESC")
    List<Object[]> getDailyClickCounts(@Param("urlMapping") UrlMapping urlMapping);
    
    @Query("SELECT c.ipAddress AS country, COUNT(c) AS count " +
           "FROM ClickStats c WHERE c.urlMapping = :urlMapping " +
           "GROUP BY c.ipAddress " +
           "ORDER BY count DESC")
    List<Object[]> getClicksByCountry(@Param("urlMapping") UrlMapping urlMapping);
    
    @Query("SELECT c.userAgent AS browser, COUNT(c) AS count " +
           "FROM ClickStats c WHERE c.urlMapping = :urlMapping " +
           "GROUP BY c.userAgent " +
           "ORDER BY count DESC")
    List<Object[]> getClicksByBrowser(@Param("urlMapping") UrlMapping urlMapping);
}
