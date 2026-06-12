SET NAMES utf8mb4;

-- ──────────────────────────────────────────────────────────
-- 1. 더미 유저 3명 생성
-- ──────────────────────────────────────────────────────────
INSERT IGNORE INTO users (nickname, email, role, status, registration_status, nickname_change_count, created_at, updated_at)
VALUES
    ('seed_user_1', 'seed1@test.com', 'USER', 'ACTIVE', 'ONBOARDING_COMPLETED', 0, NOW(), NOW()),
    ('seed_user_2', 'seed2@test.com', 'USER', 'ACTIVE', 'ONBOARDING_COMPLETED', 0, NOW(), NOW()),
    ('seed_user_3', 'seed3@test.com', 'USER', 'ACTIVE', 'ONBOARDING_COMPLETED', 0, NOW(), NOW());

-- ──────────────────────────────────────────────────────────
-- 2. 더미 카테고리 생성 (없을 때만)
-- ──────────────────────────────────────────────────────────
INSERT IGNORE INTO categories (name, description, display_order, created_at, updated_at)
VALUES
    ('업무자동화',  '반복 업무를 자동화하는 프롬프트',  1, NOW(), NOW()),
    ('글쓰기',     '글쓰기·문서 작성 프롬프트',        2, NOW(), NOW()),
    ('코딩',       '개발·코딩 관련 프롬프트',           3, NOW(), NOW()),
    ('학습',       '공부·학습 보조 프롬프트',           4, NOW(), NOW()),
    ('마케팅',     '마케팅·카피라이팅 프롬프트',        5, NOW(), NOW());

-- ──────────────────────────────────────────────────────────
-- 3. 프롬프트 10,000건 삽입 프로시저
-- ──────────────────────────────────────────────────────────
DROP PROCEDURE IF EXISTS seed_10000_prompts;

DELIMITER $$

CREATE PROCEDURE seed_10000_prompts()
BEGIN
    DECLARE i       INT DEFAULT 1;
    DECLARE uid     BIGINT;
    DECLARE cid     BIGINT;
    DECLARE u1      BIGINT;
    DECLARE u2      BIGINT;
    DECLARE u3      BIGINT;
    DECLARE c1      BIGINT;
    DECLARE c2      BIGINT;
    DECLARE c3      BIGINT;
    DECLARE c4      BIGINT;
    DECLARE c5      BIGINT;

    -- 방금 만든 유저/카테고리 ID 조회
    SELECT user_id INTO u1 FROM users WHERE nickname = 'seed_user_1' LIMIT 1;
    SELECT user_id INTO u2 FROM users WHERE nickname = 'seed_user_2' LIMIT 1;
    SELECT user_id INTO u3 FROM users WHERE nickname = 'seed_user_3' LIMIT 1;

    SELECT category_id INTO c1 FROM categories WHERE name = '업무자동화' LIMIT 1;
    SELECT category_id INTO c2 FROM categories WHERE name = '글쓰기'     LIMIT 1;
    SELECT category_id INTO c3 FROM categories WHERE name = '코딩'       LIMIT 1;
    SELECT category_id INTO c4 FROM categories WHERE name = '학습'       LIMIT 1;
    SELECT category_id INTO c5 FROM categories WHERE name = '마케팅'     LIMIT 1;

    SET autocommit = 0;

    WHILE i <= 10000 DO

            -- 유저 순환
            SET uid = CASE (i % 3)
                          WHEN 0 THEN u1
                          WHEN 1 THEN u2
                          ELSE        u3
                END;

            -- 카테고리 순환
            SET cid = CASE (i % 5)
                          WHEN 0 THEN c1
                          WHEN 1 THEN c2
                          WHEN 2 THEN c3
                          WHEN 3 THEN c4
                          ELSE        c5
                END;

            INSERT INTO prompts (user_id, category_id, title, content, status, created_at, updated_at)
            VALUES (
                       uid,
                       cid,

                       -- 제목: 검색 키워드가 자연스럽게 섞이도록
                       CONCAT(
                               ELT(1 + (i % 20),
                                   'ChatGPT로 업무 자동화하기',
                                   '코딩 실력을 높이는 프롬프트',
                                   '번역 품질을 높이는 방법',
                                   '데이터 분석 보고서 작성',
                                   'GPT로 마케팅 문구 만들기',
                                   '학습 계획 세우는 프롬프트',
                                   '이메일 자동 작성 템플릿',
                                   '코드 리뷰 요청 프롬프트',
                                   '블로그 글 초안 작성하기',
                                   'SQL 쿼리 최적화 도우미',
                                   '면접 준비 질문 생성기',
                                   '회의록 요약 자동화',
                                   'Python 디버깅 도우미',
                                   '제품 설명서 작성 템플릿',
                                   'Claude로 논문 요약하기',
                                   '업무 보고서 구조화 프롬프트',
                                   '영어 첨삭 도우미',
                                   'LLM 활용 아이디어 발상',
                                   '고객 응대 스크립트 생성',
                                   'React 컴포넌트 설계 프롬프트'
                               ),
                               ' (', i, '번)'
                       ),

                       -- 본문: 실제 서비스와 유사한 길이
                       CONCAT(
                               '## 프롬프트 설명\n\n',
                               ELT(1 + (i % 5),
                                   '이 프롬프트는 반복적인 업무를 AI로 자동화하는 데 활용할 수 있습니다. ChatGPT나 Claude에 그대로 붙여넣기 하면 됩니다.',
                                   '개발자를 위한 코딩 보조 프롬프트입니다. 코드 작성, 리뷰, 디버깅 등 다양한 상황에서 활용하세요.',
                                   '마케팅 문구와 카피라이팅에 특화된 프롬프트입니다. 타겟 고객층과 톤앤매너를 지정해서 사용하세요.',
                                   '학습과 자기계발을 위한 프롬프트 모음입니다. 개념 설명, 퀴즈 생성, 학습 계획 수립에 활용할 수 있습니다.',
                                   '문서 작성과 글쓰기를 도와주는 프롬프트입니다. 보고서, 이메일, 블로그 포스팅 등에 적용 가능합니다.'
                               ),
                               '\n\n## 사용법\n\n',
                               '1. 아래 프롬프트를 복사합니다.\n',
                               '2. AI 모델에 붙여넣기 합니다.\n',
                               '3. [대괄호] 부분을 상황에 맞게 수정합니다.\n\n',
                               '## 프롬프트\n\n',
                               '당신은 전문 ', ELT(1 + (i % 4), '개발자', '마케터', '작가', '컨설턴트'), '입니다. ',
                               '[목표]를 달성하기 위해 단계별로 설명해주세요. ',
                               '결과물은 한국어로 작성하고, 실무에서 바로 사용할 수 있는 수준으로 작성해주세요.'
                       ),

                       'PUBLIC',

                       -- 날짜: 최근 1년 내 랜덤 분포
                       DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
                       NOW()
                   );

            -- 1000건마다 커밋
            IF (i % 1000 = 0) THEN
                COMMIT;
                SELECT CONCAT(i, ' / 10000 완료') AS progress;
            END IF;

            SET i = i + 1;
        END WHILE;

    COMMIT;
    SET autocommit = 1;

    SELECT '완료: 프롬프트 10,000건 삽입' AS result;
END$$

DELIMITER ;

-- 실행
CALL seed_10000_prompts();