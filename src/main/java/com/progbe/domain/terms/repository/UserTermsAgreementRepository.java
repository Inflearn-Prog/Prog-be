package com.progbe.domain.terms.repository;

import com.progbe.domain.terms.entity.UserTermsAgreementEntity;
import com.progbe.domain.terms.entity.UserTermsAgreementId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreementEntity, UserTermsAgreementId> {
}
