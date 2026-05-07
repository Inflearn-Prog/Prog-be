package com.progbe.domain.report.service;

import com.progbe.domain.prompt.entity.PromptCommentEntity;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.repository.PromptCommentRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.report.dto.ReportCreateRequest;
import com.progbe.domain.report.dto.ReportCreateResponse;
import com.progbe.domain.report.entity.ReportEntity;
import com.progbe.domain.report.mapper.ReportMapper;
import com.progbe.domain.report.repository.ReportRepository;
import com.progbe.domain.report.type.ReportReason;
import com.progbe.domain.report.type.TargetType;
import com.progbe.domain.report.validator.ReportValidator;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

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
    @Mock
    private ReportValidator reportValidator;

    @Test
    @DisplayName("프롬프트 신고 생성 성공")
    void createReport_prompt_success() {
        // given
        Long reporterId = 1L;
        Long targetId = 10L;
        Long promptOwnerId = 2L;

        UserEntity reporter = createUser(reporterId);
        UserEntity promptOwner = createUser(promptOwnerId);
        PromptEntity prompt = createPrompt(targetId, promptOwner);

        ReportCreateRequest request = new ReportCreateRequest(
                TargetType.PROMPT, targetId, ReportReason.SPAM_AD, null
        );

        ReportEntity savedReport = ReportEntity.builder()
                .targetType(TargetType.PROMPT)
                .targetId(targetId)
                .reporter(reporter)
                .reason(ReportReason.SPAM_AD)
                .build();

        ReportCreateResponse expectedResponse = new ReportCreateResponse(
                TargetType.PROMPT, targetId, reporterId, LocalDateTime.now()
        );

        given(userRepository.findById(reporterId)).willReturn(Optional.of(reporter));
        given(promptRepository.findById(targetId)).willReturn(Optional.of(prompt));
        given(reportRepository.existsPendingReport(reporterId, TargetType.PROMPT, targetId)).willReturn(false);
        given(reportMapper.toEntity(eq(TargetType.PROMPT), eq(targetId), eq(reporter), eq(ReportReason.SPAM_AD), any()))
                .willReturn(savedReport);
        given(reportRepository.save(savedReport)).willReturn(savedReport);
        given(reportMapper.toCreateResponse(savedReport)).willReturn(expectedResponse);

        // when
        ReportCreateResponse response = reportService.createReport(reporterId, request);

        // then
        assertThat(response).isEqualTo(expectedResponse);
        verify(reportValidator).validateReportCreateRequest(request);
        verify(reportRepository).save(savedReport);
    }

    @Test
    @DisplayName("자기 프롬프트 신고 시 CANNOT_REPORT_SELF 에러")
    void createReport_selfPrompt_throwsCannotReportSelf() {
        // given
        Long reporterId = 1L;
        Long targetId = 10L;

        UserEntity reporter = createUser(reporterId);
        PromptEntity prompt = createPrompt(targetId, reporter); // 본인 소유

        ReportCreateRequest request = new ReportCreateRequest(
                TargetType.PROMPT, targetId, ReportReason.SPAM_AD, null
        );

        given(userRepository.findById(reporterId)).willReturn(Optional.of(reporter));
        given(promptRepository.findById(targetId)).willReturn(Optional.of(prompt));

        // when & then
        assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CANNOT_REPORT_SELF));
    }

    @Test
    @DisplayName("자기 댓글 신고 시 CANNOT_REPORT_SELF 에러")
    void createReport_selfComment_throwsCannotReportSelf() {
        // given
        Long reporterId = 1L;
        Long commentId = 20L;

        UserEntity reporter = createUser(reporterId);
        PromptCommentEntity comment = createComment(commentId, reporter);

        ReportCreateRequest request = new ReportCreateRequest(
                TargetType.COMMENT, commentId, ReportReason.INAPPROPRIATE_EXPRESSION, null
        );

        given(userRepository.findById(reporterId)).willReturn(Optional.of(reporter));
        given(promptCommentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.CANNOT_REPORT_SELF));
    }

    @Test
    @DisplayName("중복 신고 시 ALREADY_REPORTED 에러")
    void createReport_duplicate_throwsAlreadyReported() {
        // given
        Long reporterId = 1L;
        Long targetId = 10L;
        Long promptOwnerId = 2L;

        UserEntity reporter = createUser(reporterId);
        UserEntity promptOwner = createUser(promptOwnerId);
        PromptEntity prompt = createPrompt(targetId, promptOwner);

        ReportCreateRequest request = new ReportCreateRequest(
                TargetType.PROMPT, targetId, ReportReason.SPAM_AD, null
        );

        given(userRepository.findById(reporterId)).willReturn(Optional.of(reporter));
        given(promptRepository.findById(targetId)).willReturn(Optional.of(prompt));
        given(reportRepository.existsPendingReport(reporterId, TargetType.PROMPT, targetId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ALREADY_REPORTED));
    }

    @Test
    @DisplayName("존재하지 않는 프롬프트 신고 시 PROMPT_NOT_FOUND 에러")
    void createReport_promptNotFound_throwsPromptNotFound() {
        // given
        Long reporterId = 1L;
        Long targetId = 999L;

        UserEntity reporter = createUser(reporterId);

        ReportCreateRequest request = new ReportCreateRequest(
                TargetType.PROMPT, targetId, ReportReason.SPAM_AD, null
        );

        given(userRepository.findById(reporterId)).willReturn(Optional.of(reporter));
        given(promptRepository.findById(targetId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reportService.createReport(reporterId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PROMPT_NOT_FOUND));
    }

    // -- Helper methods --

    private UserEntity createUser(Long id) {
        UserEntity user = UserEntity.builder()
                .nickname("user" + id)
                .email("user" + id + "@test.com")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        // Reflection으로 id 설정
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

    private PromptCommentEntity createComment(Long id, UserEntity owner) {
        return PromptCommentEntity.builder()
                .id(id)
                .user(owner)
                .build();
    }
}
