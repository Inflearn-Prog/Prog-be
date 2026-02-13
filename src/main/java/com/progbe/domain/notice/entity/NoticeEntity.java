package com.progbe.domain.notice.entity;

import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notices",
        indexes = {
                @Index(name = "idx_notices_created_at", columnList = "created_at"),
                @Index(name = "idx_notices_deleted_at", columnList = "deleted_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public NoticeEntity(String title, String content) {
        this.title = title;
        this.content = content;
    }
}