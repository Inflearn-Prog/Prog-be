package com.progbe.domain.jobrole.entity;

import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "job_roles",
        indexes = {
                @Index(name = "idx_job_role_parent_id", columnList = "parent_id"),
                @Index(name = "idx_job_role_depth",     columnList = "depth"),
                @Index(name = "idx_job_role_deleted_at", columnList = "deleted_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobRoleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_role_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private JobRoleEntity parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<JobRoleEntity> children = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String name;

    // 0: 대분류, 1: 중분류, 2: 소분류
    @Column(nullable = false)
    private int depth;

    // 같은 depth 내 정렬 순서
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    // false: 선택 불가 카테고리 노드, true: 유저가 선택 가능한 리프 노드
    @Column(name = "is_selectable", nullable = false)
    private boolean selectable = true;

    @Builder
    public JobRoleEntity(JobRoleEntity parent, String name, int depth, int displayOrder, boolean selectable) {
        this.parent = parent;
        this.name = name;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.selectable = selectable;
    }

    public void update(String name, int displayOrder, boolean selectable) {
        if (name != null) this.name = name;
        this.displayOrder = displayOrder;
        this.selectable = selectable;
    }

    public void changeParent(JobRoleEntity newParent, int newDepth) {
        this.parent = newParent;
        this.depth = newDepth;
    }

    public boolean hasActiveChildren() {
        return children.stream().anyMatch(c -> c.getDeletedAt() == null);
    }
}
