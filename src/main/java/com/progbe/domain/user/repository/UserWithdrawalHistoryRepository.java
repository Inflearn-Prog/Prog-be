package com.progbe.domain.user.repository;

import com.progbe.domain.user.entity.UserWithdrawalHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWithdrawalHistoryRepository extends JpaRepository<UserWithdrawalHistoryEntity, Long> {
}
