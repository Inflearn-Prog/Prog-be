package com.progbe.domain.admin.entity;

import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "statistics",
        indexes = {
                @Index(name = "idx_statistics_type_created_at", columnList = "type, created_at")
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class StatisticEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long count;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatisticType type;
}
