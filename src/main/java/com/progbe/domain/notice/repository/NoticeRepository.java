package com.progbe.domain.notice.repository;

import com.progbe.domain.notice.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    @Modifying
    @Query("UPDATE NoticeEntity n SET n.deletedAt = :deletedAt WHERE n.id IN :ids AND n.deletedAt IS NULL")
    int bulkSoftDelete(@Param("ids") List<Long> ids, @Param("deletedAt") LocalDateTime deletedAt);

    @Query("SELECT n FROM NoticeEntity n WHERE n.deletedAt IS NULL ORDER BY n.createdAt DESC")
    Page<NoticeEntity> findAllByNotDeleted(Pageable pageable);

    @Query("SELECT n FROM NoticeEntity n WHERE n.id = :id AND n.deletedAt IS NULL")
    Optional<NoticeEntity> findByIdAndNotDeleted(@Param("id") Long id);
}