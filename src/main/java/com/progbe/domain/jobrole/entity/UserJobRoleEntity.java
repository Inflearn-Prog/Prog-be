package com.progbe.domain.jobrole.entity;

import com.progbe.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_job_roles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJobRoleEntity {

    @EmbeddedId
    private UserJobRoleId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @MapsId("jobRoleId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_role_id")
    private JobRoleEntity jobRole;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @jakarta.persistence.PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static UserJobRoleEntity of(UserEntity user, JobRoleEntity jobRole) {
        UserJobRoleEntity entity = new UserJobRoleEntity();
        entity.id = new UserJobRoleId(user.getId(), jobRole.getId());
        entity.user = user;
        entity.jobRole = jobRole;
        return entity;
    }
}
