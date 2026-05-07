package com.progbe.domain.report.service;

import com.progbe.domain.prompt.entity.CommentStatus;
import com.progbe.domain.prompt.entity.PromptCommentEntity;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.repository.PromptCommentRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.domain.report.dto.ReportProcessRequest;
import com.progbe.domain.report.dto.ReportProcessResponse;
import com.progbe.domain.report.entity.ReportEntity;
import com.progbe.domain.report.mapper.ReportMapper;
import com.progbe.domain.report.repository.ReportRepository;
import com.progbe.domain.report.type.ReportAction;
import com.progbe.domain.report.type.ReportReason;
import com.progbe.domain.report.type.TargetType;
import com.progbe.domain.report.type.ReportStatus;
import com.progbe.domain.report.type.TargetType;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @InjectMocks
    private AdminReportService adminReportService;

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private PromptRepository promptRepository;
    @Mock
    private PromptCommentRepository promptCommentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReportMapper reportMapper;

    @Nested
    @DisplayName("processReport")
    class ProcessReport {

        @Test
        @DisplayName("프롬프트 신고 수락 - PRIVATE 처리 (기본값)")
        void processReport_prompt_defaultPrivate() {
            // given
            Long reportId = 1L;
            Long targetId = 10L;

            UserEntity author = createUser(2L);
            PromptEntity prompt = createPrompt(targetId, author);
            ReportEntity report = createReportEntity(reportId, TargetType.PROMPT, targetId);

            ReportProcessRequest request = new ReportProcessRequest("관리자 메모", null);
            ReportProcessResponse expectedResponse = new ReportProcessResponse(
                    reportId, targetId, TargetType.PROMPT, LocalDateTime.now()
            );

            given(reportRepository.findByIdWithLock(reportId)).willReturn(Optional.of(report));
            given(promptRepository.findById(targetId)).willReturn(Optional.of(prompt));
            given(reportMapper.toProcessResponse(report)).willReturn(expectedResponse);

            // when
            ReportProcessResponse response = adminReportService.processReport(reportId, request);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            verify(promptRepository).save(prompt);
            verify(userRepository).save(author);
            verify(reportRepository).save(report);
        }

        @Test
        @DisplayName("프롬프트 신고 수락 - DELETE 처리")
        void processReport_prompt_deleteAction() {
            // given
            Long reportId = 1L;
            Long targetId = 10L;

            UserEntity author = createUser(2L);
            PromptEntity prompt = createPrompt(targetId, author);
            ReportEntity report = createReportEntity(reportId, TargetType.PROMPT, targetId);

            ReportProcessRequest request = new ReportProcessRequest("삭제 처리", ReportAction.DELETE);
            ReportProcessResponse expectedResponse = new ReportProcessResponse(
                    reportId, targetId, TargetType.PROMPT, LocalDateTime.now()
            );

            given(reportRepository.findByIdWithLock(reportId)).willReturn(Optional.of(report));
            given(promptRepository.findById(targetId)).willReturn(Optional.of(prompt));
            given(reportMapper.toProcessResponse(report)).willReturn(expectedResponse);

            // when
            ReportProcessResponse response = adminReportService.processReport(reportId, request);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            verify(promptRepository).save(prompt);
            verify(userRepository).save(author);
        }

        @Test
        @DisplayName("댓글 신고 수락 - softDelete + 작성자 SUSPENDED")
        void processReport_comment_softDeleteAndSuspend() {
            // given
            Long reportId = 1L;
            Long commentId = 20L;

            UserEntity commentAuthor = createUser(3L);
            PromptCommentEntity comment = PromptCommentEntity.builder()
                    .id(commentId)
                    .user(commentAuthor)
                    .comment("신고 대상 댓글")
                    .commentStatus(CommentStatus.PUBLIC)
                    .build();

            ReportEntity report = createReportEntity(reportId, TargetType.COMMENT, commentId);

            ReportProcessRequest request = new ReportProcessRequest("댓글 처리", null);
            ReportProcessResponse expectedResponse = new ReportProcessResponse(
                    reportId, commentId, TargetType.COMMENT, LocalDateTime.now()
            );

            given(reportRepository.findByIdWithLock(reportId)).willReturn(Optional.of(report));
            given(promptCommentRepository.findById(commentId)).willReturn(Optional.of(comment));
            given(reportMapper.toProcessResponse(report)).willReturn(expectedResponse);

            // when
            ReportProcessResponse response = adminReportService.processReport(reportId, request);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            assertThat(comment.getCommentStatus()).isEqualTo(CommentStatus.DELETED);
            verify(promptCommentRepository).save(comment);
            verify(userRepository).save(commentAuthor);
            verify(reportRepository).save(report);
        }

        @Test
        @DisplayName("이미 처리된 신고 재처리 시 ALREADY_PROCESSED 에러")
        void processReport_alreadyProcessed_throwsError() {
            // given
            Long reportId = 1L;
            ReportEntity report = createReportEntity(reportId, TargetType.PROMPT, 10L);
            report.process("이미 처리됨"); // status를 PROCESSED로 변경

            ReportProcessRequest request = new ReportProcessRequest("재처리 시도", null);

            given(reportRepository.findByIdWithLock(reportId)).willReturn(Optional.of(report));

            // when & then
            assertThatThrownBy(() -> adminReportService.processReport(reportId, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ALREADY_PROCESSED));
        }
    }

    @Nested
    @DisplayName("rejectReport")
    class RejectReport {

        @Test
        @DisplayName("신고 반려 성공 - 대상 변경 없음")
        void rejectReport_success() {
            // given
            Long reportId = 1L;
            ReportEntity report = createReportEntity(reportId, TargetType.PROMPT, 10L);

            ReportProcessRequest request = new ReportProcessRequest("반려 사유", null);
            ReportProcessResponse expectedResponse = new ReportProcessResponse(
                    reportId, 10L, TargetType.PROMPT, LocalDateTime.now()
            );

            given(reportRepository.findByIdWithLock(reportId)).willReturn(Optional.of(report));
            given(reportMapper.toProcessResponse(report)).willReturn(expectedResponse);

            // when
            ReportProcessResponse response = adminReportService.rejectReport(reportId, request);

            // then
            assertThat(response).isEqualTo(expectedResponse);
            assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
            verify(reportRepository).save(report);
        }

        @Test
        @DisplayName("이미 처리된 신고 반려 시 ALREADY_PROCESSED 에러")
        void rejectReport_alreadyProcessed_throwsError() {
            // given
            Long reportId = 1L;
            ReportEntity report = createReportEntity(reportId, TargetType.PROMPT, 10L);
            report.reject("이미 반려됨"); // status를 REJECTED로 변경

            ReportProcessRequest request = new ReportProcessRequest("재반려 시도", null);

            given(reportRepository.findByIdWithLock(reportId)).willReturn(Optional.of(report));

            // when & then
            assertThatThrownBy(() -> adminReportService.rejectReport(reportId, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ALREADY_PROCESSED));
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("관리자 댓글 삭제 성공")
        void deleteComment_success() {
            // given
            Long commentId = 20L;
            PromptCommentEntity comment = PromptCommentEntity.builder()
                    .id(commentId)
                    .user(createUser(3L))
                    .comment("삭제 대상 댓글")
                    .commentStatus(CommentStatus.PUBLIC)
                    .build();

            given(promptCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

            // when
            adminReportService.deleteComment(commentId);

            // then
            assertThat(comment.getCommentStatus()).isEqualTo(CommentStatus.DELETED);
            verify(promptCommentRepository).save(comment);
        }
    }

    // -- Helper methods --

    private UserEntity createUser(Long id) {
        UserEntity user = UserEntity.builder()
                .nickname("user" + id)
                .email("user" + id + "@test.com")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        try {
            var idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private PromptEntity createPrompt(Long id, UserEntity owner) {
        try {
            var constructor = PromptEntity.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            PromptEntity prompt = constructor.newInstance();

            var idField = PromptEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(prompt, id);

            var userField = PromptEntity.class.getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(prompt, owner);

            return prompt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ReportEntity createReportEntity(Long id, TargetType targetType, Long targetId) {
        UserEntity reporter = createUser(99L);
        ReportEntity report = ReportEntity.builder()
                .targetType(targetType)
                .targetId(targetId)
                .reporter(reporter)
                .reason(ReportReason.SPAM_AD)
                .build();
        try {
            var idField = ReportEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(report, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return report;
    }
}
