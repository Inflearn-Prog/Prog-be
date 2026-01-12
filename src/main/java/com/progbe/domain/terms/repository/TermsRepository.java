package com.progbe.domain.terms.repository;

import com.progbe.domain.terms.entity.TermsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermsRepository extends JpaRepository<TermsEntity, Long> {
    List<TermsEntity> findAllByOrderByRequiredDescIdAsc(); // 필수 여부 우선 정렬 등 필요시 조정
}
