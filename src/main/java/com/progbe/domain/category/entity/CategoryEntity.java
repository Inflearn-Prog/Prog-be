package com.progbe.domain.category.entity;

import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_category_name",
                        columnNames = {"name"}
                )
        },
        indexes = {
                @Index(name = "idx_category_parent_id", columnList = "parent_id"),
                @Index(name = "idx_category_display_order", columnList = "display_order")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;

    @OneToMany(mappedBy = "parent")
    private List<CategoryEntity> children = new ArrayList<>();

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Builder
    public CategoryEntity(String name, String description, CategoryEntity parent, Integer displayOrder) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    // 어드민에서 카테고리 관리 고려
    public void update(String name, String description, CategoryEntity parent) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (parent != null) {
            this.parent = parent;
        }
    }

    public boolean isParentCategory() {
        return parent == null;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}

