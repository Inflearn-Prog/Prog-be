package com.progbe.domain.qna.repository;

import com.progbe.domain.qna.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {

    @Query("SELECT a FROM AnswerEntity a WHERE a.question.id = :questionId AND a.deletedAt IS NULL")
    Optional<AnswerEntity> findByQuestionIdAndNotDeleted(@Param("questionId") Long questionId);

    @Modifying
    @Query("DELETE FROM AnswerEntity a WHERE a.deletedAt IS NOT NULL AND a.deletedAt < :threshold")
    int deleteAllByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}
