package com.progbe.domain.prompt.service;

import com.progbe.domain.prompt.dto.PromptCommentRequest;
import com.progbe.domain.prompt.dto.PromptCommentResponse;
import com.progbe.domain.prompt.entity.CommentStatus;
import com.progbe.domain.prompt.entity.PromptCommentEntity;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.repository.PromptCommentRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromptCommentService {

    private final PromptCommentRepository promptCommentRepository;

    private final UserRepository userRepository;

    private final PromptRepository promptRepository;


    public PromptCommentResponse createComment(Long userId, Long promptId, PromptCommentRequest promptCommentRequest) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PromptEntity promptEntity = promptRepository.findById(promptId).orElseThrow(() -> new CustomException(ErrorCode.PROMPT_NOT_FOUND));

        PromptCommentEntity promptCommentEntity = PromptCommentEntity.createFrom(promptEntity, userEntity, promptCommentRequest);

        promptCommentRepository.save(promptCommentEntity);

        return PromptCommentResponse.of(promptCommentEntity);
    }

    public PromptCommentResponse createReply(Long userId, Long promptId, Long commentId, PromptCommentRequest promptCommentRequest) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();

        PromptEntity promptEntity = promptRepository.findById(promptId).orElseThrow();

        PromptCommentEntity parentComment = promptCommentRepository.findById(commentId).orElseThrow();

        if (parentComment.getParentId() != null) {
            throw new CustomException(ErrorCode.REPLY_DEPTH_LIMIT);
        }

        if (!parentComment.getPrompt().getId().equals(promptId)) {
            throw new CustomException(ErrorCode.INVALID_COMMENT_PROMPT);
        }

        PromptCommentEntity promptCommentEntity = PromptCommentEntity.createFrom(promptEntity, userEntity, commentId, promptCommentRequest);

        promptCommentRepository.save(promptCommentEntity);

        return PromptCommentResponse.of(promptCommentEntity);
    }

    @Transactional
    public PromptCommentResponse modifyComment(Long userId, Long commentId, PromptCommentRequest promptCommentRequest) {

        PromptCommentEntity promptCommentEntity = promptCommentRepository.findById(commentId).orElseThrow(
                () -> new CustomException(ErrorCode.COMMENT_NOT_FOUND)
        );

        if (!promptCommentEntity.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_WRITER);
        }

        promptCommentEntity.setComment(promptCommentRequest.comment());

        return PromptCommentResponse.of(promptCommentEntity);
    }

    public void deleteComment(Long userId, Long commentId) {
        PromptCommentEntity comment = promptCommentRepository.findByIdWithUser(commentId);

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_COMMENT_WRITER);
        }

        comment.softDelete();
    }

    public Slice<PromptCommentResponse> readComments(Long promptId) {
        Slice<PromptCommentEntity> commentEntitySlice = promptCommentRepository.findAllByPromptId(promptId);

        return PromptCommentResponse.sliceOf(commentEntitySlice);
    }
}
