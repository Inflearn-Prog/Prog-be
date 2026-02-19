package com.progbe.domain.report.repository;

import com.progbe.domain.report.dto.PendingReportDto;
import com.progbe.domain.report.entity.ReportEntity;
import com.progbe.domain.report.type.ReportStatus;
import com.progbe.domain.report.type.TargetType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    @Query("SELECT COUNT(r) > 0 FROM ReportEntity r " +
           "WHERE r.reporter.id = :reporterId " +
           "AND r.targetType = :targetType " +
           "AND r.targetId = :targetId " +
           "AND r.status = com.progbe.domain.report.type.ReportStatus.PENDING")
    boolean existsPendingReport(@Param("reporterId") Long reporterId, 
                                @Param("targetType") TargetType targetType, 
                                @Param("targetId") Long targetId);

    @Query("SELECT r FROM ReportEntity r " +
           "WHERE r.status = :status " +
           "ORDER BY r.createdAt DESC")
    Page<ReportEntity> findByStatusOrderByCreatedAtDesc(@Param("status") ReportStatus status, Pageable pageable);

    @Query("SELECT new com.progbe.domain.report.dto.PendingReportDto(" +
           "r.id, r.createdAt, r.reason, " +
           "COALESCE(p.title, '(삭제된 게시글)'), " +
           "u.nickname, " +
           "CASE WHEN r.reason = com.progbe.domain.report.type.ReportReason.OTHER THEN r.reasonDetail ELSE '' END, " +
           "r.status) " +
           "FROM ReportEntity r " +
           "JOIN r.reporter u " +
           "LEFT JOIN PromptEntity p ON r.targetId = p.id AND r.targetType = com.progbe.domain.report.type.TargetType.PROMPT " +
           "WHERE r.status = :status " +
           "ORDER BY r.createdAt DESC")
    Page<PendingReportDto> findPendingReportsAsDto(@Param("status") ReportStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReportEntity r WHERE r.id = :id")
    Optional<ReportEntity> findByIdWithLock(@Param("id") Long id);
}
