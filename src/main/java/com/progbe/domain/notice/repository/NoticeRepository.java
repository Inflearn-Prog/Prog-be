package com.progbe.domain.notice.repository;

import com.progbe.domain.notice.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

    @Modifying
    @Query("UPDATE NoticeEntity n SET n.deletedAt = :deletedAt WHERE n.id IN :ids AND n.deletedAt IS NULL")
    int bulkSoftDelete(@Param("ids") List<Long> ids, @Param("deletedAt") LocalDateTime deletedAt);
}