package com.progbe.domain.prompt.repository;

import com.progbe.domain.admin.dto.PromptAdminDto;
import com.progbe.domain.admin.dto.UserCountDto;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.type.PromptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query("UPDATE PromptEntity p SET p.status = :status WHERE p.id IN :promptIds")
    int bulkUpdateStatus(@Param("promptIds") List<Long> promptIds, @Param("status") PromptStatus status);

    @Modifying
    @Query("UPDATE PromptEntity p SET p.category.id = :categoryId WHERE p.id IN :promptIds")
    int bulkUpdateCategory(@Param("promptIds") List<Long> promptIds, @Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE PromptEntity p SET p.deletedAt = :deletedAt, p.status = :status WHERE p.id IN :promptIds")
    int bulkSoftDelete(@Param("promptIds") List<Long> promptIds, @Param("deletedAt") LocalDateTime deletedAt, @Param("status") PromptStatus status);

    @Query("SELECT new PromptAdminDto(" +
            "p.id, p.title, u.nickname, c.name, p.status, p.createdAt) " +
            "FROM PromptEntity p " +
            "JOIN p.user u " +
            "JOIN p.category c " +
            "WHERE (:keyword IS NULL OR p.title LIKE %:keyword%) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "ORDER BY p.createdAt DESC")
    Page<PromptAdminDto> findByFiltersAsDto(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") PromptStatus status,
            Pageable pageable
    );

    @Query("SELECT p.id FROM PromptEntity p WHERE p.id IN :promptIds")
    List<Long> findExistingPromptIds(@Param("promptIds") List<Long> promptIds);
}
