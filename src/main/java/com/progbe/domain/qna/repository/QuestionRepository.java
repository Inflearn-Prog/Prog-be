package com.progbe.domain.qna.repository;

import com.progbe.domain.qna.entity.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    @Query("SELECT q FROM QuestionEntity q WHERE q.deletedAt IS NULL ORDER BY q.createdAt DESC")
    Page<QuestionEntity> findAllByNotDeleted(Pageable pageable);

    @Query("SELECT q FROM QuestionEntity q WHERE q.id = :id AND q.deletedAt IS NULL")
    Optional<QuestionEntity> findByIdAndNotDeleted(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM QuestionEntity q WHERE q.deletedAt IS NOT NULL AND q.deletedAt < :threshold")
    int deleteAllByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}
