
-- 프롬프트 삭제 (seed 유저가 작성한 것만)
DELETE p FROM prompts p
                  JOIN users u ON u.user_id = p.user_id
WHERE u.nickname IN ('seed_user_1', 'seed_user_2', 'seed_user_3');

-- 유저 삭제
DELETE FROM users WHERE nickname IN ('seed_user_1', 'seed_user_2', 'seed_user_3');

-- 카테고리 삭제 (seed 카테고리가 다른 프롬프트에서 쓰이지 않을 때만)
DELETE FROM categories
WHERE name IN ('업무자동화', '글쓰기', '코딩', '학습', '마케팅')
  AND NOT EXISTS (
    SELECT 1 FROM prompts p WHERE p.category_id = categories.category_id
);

-- 프로시저 삭제
DROP PROCEDURE IF EXISTS seed_10000_prompts;

SELECT '완료: 목데이터 삭제' AS result;