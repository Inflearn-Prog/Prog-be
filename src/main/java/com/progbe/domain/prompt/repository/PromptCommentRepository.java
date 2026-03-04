package com.progbe.domain.prompt.repository;

import com.progbe.domain.admin.dto.UserCountDto;
import com.progbe.domain.prompt.entity.PromptCommentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromptCommentRepository extends JpaRepository<PromptCommentEntity, Long> {

    @Query("SELECT COUNT(c) FROM PromptCommentEntity c WHERE c.user.id = :userId AND c.deletedAt IS NULL")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT c.user.id as userId, COUNT(c) as count FROM PromptCommentEntity c " +
            "WHERE c.user.id IN :userIds AND c.deletedAt IS NULL " +
            "GROUP BY c.user.id")
    List<UserCountDto> countByUserIdsGrouped(@Param("userIds") List<Long> userIds);

    @Query("SELECT pc FROM PromptCommentEntity pc JOIN FETCH pc.user WHERE pc.id = :commentId")
    PromptCommentEntity findByIdWithUser(@Param("commentId") Long commentId);

    @Query("SELECT c FROM PromptCommentEntity c " +
            "JOIN FETCH c.user " +
            "WHERE c.prompt.id = :promptId AND c.deletedAt IS NULL " +
            "ORDER BY COALESCE(c.parentId, c.id) ASC, c.createdAt ASC")
    Slice<PromptCommentEntity> findAllByPromptId(Long promptId);

    List<PromptCommentEntity> findAllByDeletedAtBefore(LocalDateTime dateTime);
}
