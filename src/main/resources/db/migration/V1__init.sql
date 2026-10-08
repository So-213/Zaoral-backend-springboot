-- 業務テーブル(Flyway所有)。認証テーブル("User"等)はPrisma所有のため、ここでは作成しない。
-- 現行Next.js実装(prisma/schema.prisma)の仕様を移植。参照: ../../../../../../../../Zaoral-migration-docs/05_DB切り分け.md
-- 実行順序が重要: Prismaが先に"User"を作成済みであること(7-2参照)。

-- plan / left_projects を "User" から切り出したテーブル
CREATE TABLE user_quota (
    user_id       TEXT PRIMARY KEY REFERENCES "User"(id) ON DELETE CASCADE,
    plan          VARCHAR(16) NOT NULL DEFAULT 'FREE' CHECK (plan IN ('FREE', 'PREMIUM')),
    left_projects INTEGER     NOT NULL DEFAULT 3
);

-- 基底テーブル(共通属性)。IDはアプリケーション側で生成し、DEFAULTは持たせない。
CREATE TABLE projects (
    id         TEXT PRIMARY KEY,
    user_id    TEXT NOT NULL REFERENCES "User"(id) ON DELETE CASCADE,
    user_name  TEXT NOT NULL,
    type       VARCHAR(16) NOT NULL DEFAULT 'message' CHECK (type IN ('message', 'picture')),
    slug       TEXT NOT NULL UNIQUE,
    name       TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_projects_user_created ON projects (user_id, created_at DESC);

-- 子テーブル(message タイプ専用属性)
CREATE TABLE projects_message (
    project_id TEXT PRIMARY KEY REFERENCES projects(id) ON DELETE CASCADE,
    message    TEXT NOT NULL
);

-- 子テーブル(picture タイプ専用属性)。picture型は現在停止中だがテーブルは用意する。
CREATE TABLE projects_picture (
    project_id TEXT PRIMARY KEY REFERENCES projects(id) ON DELETE CASCADE,
    s3_key     TEXT NOT NULL
);
