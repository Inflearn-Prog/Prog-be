package com.progbe.domain.user.entity;

import com.progbe.domain.user.converter.CareerStatusListConverter;
import com.progbe.domain.user.converter.JobRoleListConverter;
import com.progbe.domain.user.converter.StringListConverter;
import com.progbe.domain.user.type.CareerStatus;
import com.progbe.domain.user.type.EducationLevel;
import com.progbe.domain.user.type.JobRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserProfileEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Convert(converter = CareerStatusListConverter.class)
    @Column(name = "current_status", columnDefinition = "json")
    private List<CareerStatus> currentStatus;

    @Convert(converter = JobRoleListConverter.class)
    @Column(name = "target_job", columnDefinition = "json")
    private List<JobRole> targetJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "education")
    private EducationLevel education;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "major")
    private String major;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Convert(converter = StringListConverter.class)
    @Column(name = "keywords", columnDefinition = "json")
    private List<String> keywords;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserProfileEntity(UserEntity user, List<CareerStatus> currentStatus, List<JobRole> targetJob, EducationLevel education, Integer experienceYears, String major, String bio, List<String> keywords) {
        this.user = user;
        this.currentStatus = currentStatus;
        this.targetJob = targetJob;
        this.education = education;
        this.experienceYears = experienceYears;
        this.major = major;
        this.bio = bio;
        this.keywords = keywords;
    }

    public void updateCareerInfo(List<CareerStatus> currentStatus, List<JobRole> targetJob) {
        this.currentStatus = currentStatus;
        this.targetJob = targetJob;
    }

    public void updateBasicInfo(EducationLevel education, String major, Integer experienceYears) {
        this.education = education;
        this.major = major;
        this.experienceYears = experienceYears;
    }
}
