package com.progbe.domain.prompt.service;

import com.progbe.domain.prompt.dto.PromptCommentRequest;
import com.progbe.domain.prompt.dto.PromptCommentResponse;
import com.progbe.domain.prompt.entity.PromptCommentEntity;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.repository.PromptCommentRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptCommentService {

    private final PromptCommentRepository promptCommentRepository;

    private final UserRepository userRepository;

    private final PromptRepository promptRepository;


    public PromptCommentResponse createComment(Long userId, Long promptId, PromptCommentRequest promptCommentRequest) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();

        PromptEntity promptEntity = promptRepository.findById(promptId).orElseThrow();

        PromptCommentEntity promptCommentEntity = PromptCommentEntity.createFrom(promptEntity, userEntity, promptCommentRequest);

        promptCommentRepository.save(promptCommentEntity);

        return PromptCommentResponse.of(promptCommentEntity);
    }

    public PromptCommentResponse createReply(Long userId, Long promptId, Long commentId, PromptCommentRequest promptCommentRequest) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();

        PromptEntity promptEntity = promptRepository.findById(promptId).orElseThrow();

        PromptCommentEntity parentComment = promptCommentRepository.findById(commentId).orElseThrow();

        if (parentComment.getParentId() != null) {
            throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다. (1-depth 제한)");
        }

        if (!parentComment.getPrompt().getId().equals(promptId)) {
            throw new IllegalArgumentException("해당 프롬프트의 댓글이 아닙니다.");
        }

        PromptCommentEntity promptCommentEntity = PromptCommentEntity.createFrom(promptEntity, userEntity, commentId, promptCommentRequest);

        promptCommentRepository.save(promptCommentEntity);

        return PromptCommentResponse.of(promptCommentEntity);
    }

    @Transactional
    public PromptCommentResponse modifyComment(Long userId, Long commentId, PromptCommentRequest promptCommentRequest) {

        if (checkWriter(userId, commentId)) {
            throw new IllegalArgumentException("댓글 작성자가 아닙니다.");
        }

        PromptCommentEntity promptCommentEntity = promptCommentRepository.findById(commentId).orElseThrow();

        promptCommentEntity.setComment(promptCommentRequest.comment());

        return PromptCommentResponse.of(promptCommentEntity);
    }

    public void deleteComment(Long userId, Long commentId) {

        if (checkWriter(userId, commentId)) {
            throw new IllegalArgumentException("댓글 작성자가 아닙니다.");
        }

        promptCommentRepository.deleteById(commentId);
    }

    public List<PromptCommentResponse> readComments(Long promptId) {
        List<PromptCommentEntity> commentEntityList = promptCommentRepository.findAllByPromptId(promptId);

        return PromptCommentResponse.listOf(commentEntityList);
    }

    private boolean checkWriter(Long userId, Long commentId) {
        PromptCommentEntity commentEntity = promptCommentRepository.findById(commentId).orElseThrow();

        Long userIdByCommentEntity = commentEntity.getUser().getId();

        return !userId.equals(userIdByCommentEntity);
    }
}
