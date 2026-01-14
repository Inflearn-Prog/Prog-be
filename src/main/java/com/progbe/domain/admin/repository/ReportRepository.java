package com.progbe.domain.admin.repository;

import com.progbe.domain.admin.entity.ReportEntity;
import com.progbe.domain.admin.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> 
{
    List<ReportEntity> findByStatus(ReportStatus status);
}
