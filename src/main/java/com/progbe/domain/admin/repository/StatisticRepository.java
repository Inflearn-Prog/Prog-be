package com.progbe.domain.admin.repository;

import com.progbe.domain.admin.entity.StatisticEntity;
import com.progbe.domain.admin.entity.StatisticType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StatisticRepository extends JpaRepository<StatisticEntity, Long>
{
    @Modifying
    @Query("""
    UPDATE StatisticEntity s
    SET s.count = s.count + 1
    WHERE s.type = :type
      AND s.createdAt >= :startOfDay
      AND s.createdAt < :endOfDay
""")
    int increaseTodayCount(
            @Param("type") StatisticType type,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
    SELECT COALESCE(SUM(s.count), 0)
    FROM StatisticEntity s
    WHERE s.type = :type
      AND s.createdAt = :createdAt
""")
    long findTotalByTypeAndCreatedAt(
            @Param("type") StatisticType type,
            @Param("createdAt") LocalDateTime createdAt
    );

}
