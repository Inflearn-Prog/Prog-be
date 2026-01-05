package com.progbe.domain.user.repository;

import com.progbe.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM User u JOIN u.socialLinks s WHERE s.provider = :provider AND s.providerUserId = :providerUserId")
    Optional<UserEntity> findBySocialProviderAndId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);

    boolean existsByNickname(String nickname);
}