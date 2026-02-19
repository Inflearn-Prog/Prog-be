package com.progbe.domain.prompt.repository;

import com.progbe.domain.admin.dto.UserCountDto;
import com.progbe.domain.prompt.entity.PromptEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromptRepository extends JpaRepository<PromptEntity, Long> {

    @Query("SELECT p FROM PromptEntity p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<PromptEntity> findByIdAndNotDeleted(@Param("id") Long id);

    @Query("SELECT p FROM PromptEntity p WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<PromptEntity> findAllByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT p FROM PromptEntity p WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<PromptEntity> findAllByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM PromptEntity p WHERE p.user.id = :userId AND p.deletedAt IS NULL")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT p.user.id as userId, COUNT(p) as count FROM PromptEntity p " +
            "WHERE p.user.id IN :userIds AND p.deletedAt IS NULL " +
            "GROUP BY p.user.id")
    List<UserCountDto> countByUserIdsGrouped(@Param("userIds") List<Long> userIds);

    @Query("SELECT p FROM PromptEntity p " +
            "JOIN FETCH p.category " +
            "WHERE p.deletedAt IS NULL " +
            "ORDER BY p.createdAt DESC")
    Page<PromptEntity> findPromptSortedTime(Pageable pageable);

    @Query("SELECT p FROM PromptEntity p " +
            "JOIN FETCH p.category " +
            "LEFT JOIN PromptLikeEntity pl ON pl.prompt = p " +
            "WHERE p.deletedAt IS NULL " +
            "GROUP BY p.id, p.category.id " +
            "ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    Page<PromptEntity> findPromptSortedLikeCount(Pageable pageable);

    @Query("SELECT p FROM PromptEntity p " +
            "JOIN FETCH p.category " +
            "LEFT JOIN PromptLikeEntity pl ON pl.prompt = p " +
            "AND pl.createdAt >= :start AND pl.createdAt <= :end " +
            "WHERE p.deletedAt IS NULL " +
            "GROUP BY p.id, p.category.id " +
            "ORDER BY COUNT(pl) DESC, p.createdAt DESC")
    List<PromptEntity> findDailyHotPrompts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}

