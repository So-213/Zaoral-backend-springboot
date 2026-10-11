-- テストユーザーを削除する。
-- user_quota / projects は "User" への外部キーが ON DELETE CASCADE のため、あわせて消える。

DELETE FROM "User" WHERE id = 'test-user-1';
