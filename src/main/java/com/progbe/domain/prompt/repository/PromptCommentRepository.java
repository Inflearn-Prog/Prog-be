package com.progbe.domain.prompt.repository;

import com.progbe.domain.admin.dto.UserCountDto;
import com.progbe.domain.prompt.entity.PromptCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromptCommentRepository extends JpaRepository<PromptCommentEntity, Long> {

    @Query("SELECT COUNT(c) FROM PromptCommentEntity c WHERE c.user.id = :userId AND c.deletedAt IS NULL")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT c.user.id as userId, COUNT(c) as count FROM PromptCommentEntity c " +
            "WHERE c.user.id IN :userIds AND c.deletedAt IS NULL " +
            "GROUP BY c.user.id")
    List<UserCountDto> countByUserIdsGrouped(@Param("userIds") List<Long> userIds);
}
