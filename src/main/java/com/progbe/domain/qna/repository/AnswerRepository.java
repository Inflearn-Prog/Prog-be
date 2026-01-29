package com.progbe.domain.qna.repository;

import com.progbe.domain.qna.entity.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {
    // 하나의 질문에 답변이 무조건 있다고 가정할 수 없기 때문에 Optional 걸었습니다.
    Optional<AnswerEntity> findByQuestionId(Long questionId);
}
