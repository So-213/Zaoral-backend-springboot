-- 動作確認用のテストユーザーを "User" に作成する(冪等)。
-- 本来 "User" はNextAuthがログイン時に作るが、フロントと結合する前はこれで代用する。
-- updated_at はPrismaがアプリ側で入れる列でDBに既定値が無いため、明示する。
-- user_quota は作らない。作成は POST /api/internal/user-quota で行う(サインアップ時の実際の流れと同じ)。

INSERT INTO "User" (id, name, email, updated_at)
VALUES ('test-user-1', 'Test User', 'test@example.com', now())
ON CONFLICT (id) DO NOTHING;
