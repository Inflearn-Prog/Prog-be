package com.progbe.domain.terms.entity;

import com.progbe.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_terms_agreements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@IdClass(UserTermsAgreementId.class)
public class UserTermsAgreementEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "terms_id")
    private Long termsId;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @MapsId("termsId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private TermsEntity terms;

    @Column(nullable = false)
    private LocalDateTime version;

    @Column(name = "is_agreed", nullable = false)
    private Boolean isAgreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public UserTermsAgreementEntity(UserEntity user, TermsEntity terms, LocalDateTime version, Boolean isAgreed) {
        this.user = user;
        this.terms = terms;
        this.version = version;
        this.isAgreed = isAgreed;
        this.agreedAt = LocalDateTime.now();
    }
}
