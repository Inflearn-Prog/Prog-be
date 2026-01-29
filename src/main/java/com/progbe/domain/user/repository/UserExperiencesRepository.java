package com.progbe.domain.user.repository;

import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserExperiencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserExperiencesRepository extends JpaRepository<UserExperiencesEntity, Long> {

    List<UserExperiencesEntity> findByUserOrderByCreatedAtAsc(UserEntity user);

    void deleteByUser(UserEntity user);
}