package com.progbe.domain.prompt.scheduler;

import com.progbe.domain.prompt.repository.PromptCommentRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.qna.repository.AnswerRepository;
import com.progbe.domain.qna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SoftDeleteCleanupScheduler {

    private static final int RETENTION_DAYS = 30;

    private final PromptRepository promptRepository;
    private final PromptCommentRepository promptCommentRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupSoftDeletedData() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);

        log.info("Soft-deleted 데이터 물리 삭제 시작 ({}일 경과 기준)", RETENTION_DAYS);

        int deletedComments = promptCommentRepository.deleteAllByDeletedAtBefore(threshold);
        log.info("삭제된 댓글: {}건", deletedComments);

        int deletedPrompts = promptRepository.deleteAllByDeletedAtBefore(threshold);
        log.info("삭제된 프롬프트: {}건", deletedPrompts);

        int deletedAnswers = answerRepository.deleteAllByDeletedAtBefore(threshold);
        log.info("삭제된 답변: {}건", deletedAnswers);

        int deletedQuestions = questionRepository.deleteAllByDeletedAtBefore(threshold);
        log.info("삭제된 문의: {}건", deletedQuestions);

        log.info("Soft-deleted 데이터 물리 삭제 완료");
    }
}
