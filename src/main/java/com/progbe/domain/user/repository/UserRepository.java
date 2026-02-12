package com.progbe.domain.user.repository;

import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u JOIN u.socialLinks s WHERE s.provider = :provider AND s.providerUserId = :providerUserId")
    Optional<UserEntity> findBySocialProviderAndId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);


    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.socialLinks WHERE u.id = :id")
    Optional<UserEntity> findByIdWithSocialLinks(@Param("id") Long id);

    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM UserEntity u WHERE u.nickname LIKE %:keyword% ORDER BY u.createdAt DESC")
    Page<UserEntity> findByNicknameContaining(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE UserEntity u SET u.status = :status WHERE u.id IN :userIds")
    int bulkUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") UserStatus status);

    @Modifying
    @Query("UPDATE UserEntity u SET u.role = :role WHERE u.id IN :userIds")
    int bulkUpdateRole(@Param("userIds") List<Long> userIds, @Param("role") Role role);

    @Query("SELECT u.id FROM UserEntity u WHERE u.id IN :userIds")
    List<Long> findExistingUserIds(@Param("userIds") List<Long> userIds);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
}