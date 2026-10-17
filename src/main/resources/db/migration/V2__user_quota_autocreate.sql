-- "User" 行の作成に user_quota 行の作成を追従させる(将来やること.md B-1)。
-- 参照: ../../../../../../../../Zaoral-migration-docs/05_DB切り分け.md の「受容したリスク」
--
-- 背景: user_quota はサインアップ時に Next.js → Spring Boot の初期化API呼び出しで作られていた。
-- この呼び出しは "User" 行のコミット後に実行されるため、失敗しても "User" はロールバックされず、
-- 「"User" はあるが user_quota が無い」ユーザーが生まれ、自動回復しなかった(dual-write問題)。
--
-- 対処: 行の作成を "User" への INSERT と同一トランザクションに入れ、DB自身に実行させる。
-- user_quota.user_id の ON DELETE CASCADE が削除方向を保証しているのと対になる、作成方向の保証。
-- SQLに ON INSERT CASCADE は存在しないためトリガで実現する。
--
-- 注意: このトリガは Prisma 所有の "User" テーブルに付く。prisma migrate reset 等で
-- "User" を作り直すとトリガも消えるため、その後は Flyway を流し直す必要がある。

CREATE FUNCTION create_user_quota() RETURNS TRIGGER AS $$
BEGIN
    -- plan / left_projects は指定しない。初期値は user_quota 側の DEFAULT を単一の正とする。
    INSERT INTO user_quota (user_id) VALUES (NEW.id)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- AFTER INSERT は「コミット後」ではなく、同一トランザクション内でその行のINSERT直後に動く。
-- したがってトリガが失敗すれば "User" の INSERT ごとロールバックされる。
CREATE TRIGGER user_quota_autocreate
AFTER INSERT ON "User"
FOR EACH ROW EXECUTE FUNCTION create_user_quota();

-- トリガ導入前に発生していた取りこぼしを埋める。
INSERT INTO user_quota (user_id)
SELECT u.id FROM "User" u
LEFT JOIN user_quota q ON q.user_id = u.id
WHERE q.user_id IS NULL;
