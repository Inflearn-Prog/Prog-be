package com.progbe.domain.jobrole.repository;

import com.progbe.domain.jobrole.entity.UserJobRoleEntity;
import com.progbe.domain.jobrole.entity.UserJobRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserJobRoleRepository extends JpaRepository<UserJobRoleEntity, UserJobRoleId> {

    @Query("SELECT ujr FROM UserJobRoleEntity ujr JOIN FETCH ujr.jobRole WHERE ujr.id.userId = :userId")
    List<UserJobRoleEntity> findByUserIdWithJobRole(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserJobRoleEntity ujr WHERE ujr.id.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
