package com.progbe.domain.admin.repository;

import com.progbe.domain.admin.entity.StatisticEntity;
import com.progbe.domain.admin.entity.StatisticType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticRepository extends JpaRepository<StatisticEntity, Long> {

    @Query("""
                SELECT COUNT(DISTINCT s.type)
                FROM StatisticEntity s
                WHERE s.createdAt >= :startDateTime
                  AND s.createdAt < :endDateTime
            """)
    long countDistinctTypesByDate(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("""
                SELECT s.type AS type, COALESCE(SUM(s.count), 0) AS count
                FROM StatisticEntity s
                WHERE s.createdAt >= :startDateTime
                  AND s.createdAt < :endDateTime
                GROUP BY s.type
            """)
    List<StatisticTypeCount> sumByTypeGrouped(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    interface StatisticTypeCount {
        StatisticType getType();
        Long getCount();
    }
}
