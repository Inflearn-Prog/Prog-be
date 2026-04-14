package com.progbe.domain.terms.repository;

import com.progbe.domain.terms.entity.UserTermsAgreementEntity;
import com.progbe.domain.terms.entity.UserTermsAgreementId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreementEntity, UserTermsAgreementId> {
    
    @Query("SELECT uta FROM UserTermsAgreementEntity uta " +
           "JOIN FETCH uta.terms " +
           "WHERE uta.userId = :userId AND uta.isAgreed = true AND uta.withdrawnAt IS NULL")
    List<UserTermsAgreementEntity> findAllByUserIdAndIsAgreedTrueWithTerms(@Param("userId") Long userId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uta FROM UserTermsAgreementEntity uta WHERE uta.userId = :userId AND uta.termsId = :termsId")
    Optional<UserTermsAgreementEntity> findByUserIdAndTermsIdForUpdate(@Param("userId") Long userId, @Param("termsId") Long termsId);
    
    Optional<UserTermsAgreementEntity> findByUserIdAndTermsId(Long userId, Long termsId);
}
