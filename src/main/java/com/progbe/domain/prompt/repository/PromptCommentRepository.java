package com.progbe.domain.prompt.repository;

import com.progbe.domain.admin.dto.UserCountDto;
import com.progbe.domain.prompt.dto.PromptCommentCountDto;
import com.progbe.domain.prompt.entity.PromptCommentEntity;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromptCommentRepository extends JpaRepository<PromptCommentEntity, Long> {
    @Query("SELECT c FROM PromptCommentEntity c " +
            "JOIN FETCH c.user " +
            "WHERE c.prompt.id = :promptId " +
            "ORDER BY COALESCE(c.parentId, c.id) ASC, c.createdAt ASC")
    List<PromptCommentEntity> findAllByPromptId(Long promptId);

    @Query("SELECT COUNT(c) FROM PromptCommentEntity c WHERE c.user.id = :userId AND c.deletedAt IS NULL")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT c.user.id as userId, COUNT(c) as count FROM PromptCommentEntity c " +
            "WHERE c.user.id IN :userIds AND c.deletedAt IS NULL " +
            "GROUP BY c.user.id")
    List<UserCountDto> countByUserIdsGrouped(@Param("userIds") List<Long> userIds);

    // 목록 화면에서 프롬프트별 댓글 수를 한 번에 채운다(N+1 방지). 삭제된 댓글은 세지 않는다.
    @Query("SELECT c.prompt.id as promptId, COUNT(c) as count FROM PromptCommentEntity c " +
            "WHERE c.prompt.id IN :promptIds AND c.deletedAt IS NULL " +
            "GROUP BY c.prompt.id")
    List<PromptCommentCountDto> countByPromptIds(@Param("promptIds") List<Long> promptIds);

    @Query("SELECT pc FROM PromptCommentEntity pc JOIN FETCH pc.user WHERE pc.id = :commentId")
    PromptCommentEntity findByIdWithUser(@Param("commentId") Long commentId);

    @Query("SELECT c FROM PromptCommentEntity c " +
            "JOIN FETCH c.user " +
            "WHERE c.prompt.id = :promptId AND c.deletedAt IS NULL " +
            "ORDER BY COALESCE(c.parentId, c.id) ASC, c.createdAt ASC")
    Slice<PromptCommentEntity> findSliceAllByPromptId(Long promptId);

    List<PromptCommentEntity> findAllByDeletedAtBefore(LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM PromptCommentEntity c WHERE c.deletedAt IS NOT NULL AND c.deletedAt < :threshold")
    int deleteAllByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}
