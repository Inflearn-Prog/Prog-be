package com.progbe.domain.jobrole.repository;

import com.progbe.domain.jobrole.entity.JobRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRoleRepository extends JpaRepository<JobRoleEntity, Long> {

    // 활성 직무 전체를 depth 기준 오름차순 조회 (부모 먼저 나오도록)
    @Query("SELECT j FROM JobRoleEntity j WHERE j.deletedAt IS NULL ORDER BY j.depth ASC, j.displayOrder ASC")
    List<JobRoleEntity> findAllActive();

    @Query("SELECT j FROM JobRoleEntity j WHERE j.id = :id AND j.deletedAt IS NULL")
    Optional<JobRoleEntity> findByIdActive(@Param("id") Long id);

    @Query("SELECT j FROM JobRoleEntity j WHERE j.id IN :ids AND j.deletedAt IS NULL")
    List<JobRoleEntity> findAllActiveByIds(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(j) > 0 FROM JobRoleEntity j WHERE j.parent.id = :parentId AND j.deletedAt IS NULL")
    boolean existsActiveChildrenByParentId(@Param("parentId") Long parentId);



}
