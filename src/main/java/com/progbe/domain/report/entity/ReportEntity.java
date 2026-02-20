package com.progbe.domain.report.entity;

import com.progbe.domain.report.type.ReportReason;
import com.progbe.domain.report.type.ReportStatus;
import com.progbe.domain.report.type.TargetType;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reports",
        indexes = {
                @Index(name = "idx_reports_status", columnList = "status"),
                @Index(name = "idx_reports_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_reports_target", columnList = "target_type, target_id"),
                @Index(name = "idx_reports_reporter_target_status", columnList = "reporter_id, target_type, target_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private UserEntity reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportReason reason;

    @Column(name = "reason_detail", length = 200)
    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "admin_remark", length = 1000)
    private String adminRemark;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder
    public ReportEntity(TargetType targetType, Long targetId, UserEntity reporter, 
                       ReportReason reason, String reasonDetail) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.reporter = reporter;
        this.reason = reason;
        this.reasonDetail = reasonDetail;
    }

    public void process(String adminRemark) {
        this.status = ReportStatus.PROCESSED;
        this.adminRemark = adminRemark;
        this.processedAt = LocalDateTime.now();
    }
}
