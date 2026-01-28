package com.progbe.domain.user.repository;

import com.progbe.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u JOIN u.socialLinks s WHERE s.provider = :provider AND s.providerUserId = :providerUserId")
    Optional<UserEntity> findBySocialProviderAndId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);


    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.socialLinks WHERE u.id = :id")
    Optional<UserEntity> findByIdWithSocialLinks(@Param("id") Long id);

    boolean existsByNickname(String nickname);

    //명확한 검색 조건을 모르겠어서 일단은 피그마에 보이는 대로 구현했습니다
    Page<UserEntity> findByEmailContainingOrNicknameContaining(
            String email,
            String nickname,
            Pageable pageable
    );
}