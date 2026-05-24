package com.progbe.domain.prompt.repository;

import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.entity.PromptLikeEntity;
import com.progbe.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptLikeRepository extends JpaRepository<PromptLikeEntity, Long> {
    Optional<PromptLikeEntity> findByUserAndPrompt(UserEntity user, PromptEntity prompt);

    boolean existsByUserIdAndPromptId(Long userId, Long promptId);

    long countByPromptId(Long promptId);

    @Query("SELECT pl.prompt.id FROM PromptLikeEntity pl WHERE pl.user.id = :userId AND pl.prompt.id IN :promptIds")
    List<Long> findLikedPromptIdsByUserId(@Param("userId") Long userId, @Param("promptIds") List<Long> promptIds);

    @Query(value = "SELECT p FROM PromptLikeEntity pl " +
            "JOIN pl.prompt p " +
            "JOIN FETCH p.user " +
            "JOIN FETCH p.category " +
            "WHERE pl.user.id = :userId " +
            "AND p.deletedAt IS NULL AND p.status <> 'DELETED' " +
            "ORDER BY pl.createdAt DESC",
            countQuery = "SELECT COUNT(pl) FROM PromptLikeEntity pl " +
            "WHERE pl.user.id = :userId " +
            "AND pl.prompt.deletedAt IS NULL AND pl.prompt.status <> 'DELETED'")
    Page<PromptEntity> findLikedPromptsByUserId(@Param("userId") Long userId, Pageable pageable);
}
