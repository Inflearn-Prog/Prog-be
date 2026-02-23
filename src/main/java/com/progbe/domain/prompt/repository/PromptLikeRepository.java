package com.progbe.domain.prompt.repository;

import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.entity.PromptLikeEntity;
import com.progbe.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromptLikeRepository extends JpaRepository<PromptLikeEntity, Long> {
    Optional<PromptLikeEntity> findByUserAndPrompt(UserEntity user, PromptEntity prompt);
}
