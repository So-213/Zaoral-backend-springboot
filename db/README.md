# db/

ローカル開発用のSQLスクリプト。アプリ本体には含まれない。

スキーマのマイグレーションはこのリポジトリでは管理していない。DB作成プロセス(`Zaoral-frontend`
の `ops/db/migration/`)が唯一の適用主体で、このアプリはDDLを打たない。詳細は
`Zaoral-frontend/ops/db/README.md` 参照。

| ファイル | 内容 |
|---|---|
| `create_test_user.sql` | テストユーザー `test-user-1` を作成(冪等) |
| `delete_test_user.sql` | テストユーザーを削除(quota・プロジェクトもCascadeで消える) |

## 実行方法

ローカルPostgresは `Zaoral-frontend` の `docker-compose.dev.yml` で起動しているコンテナ(`zaoral-frontend-db-1`)。

**Git Bash から**

```sh
docker exec -i zaoral-frontend-db-1 psql -U zaoral -d zaoral < db/create_test_user.sql
```

**pgAdmin から**

Query Tool でファイルを開いて実行する。

PowerShell 5.1 のパイプで流すと日本語が化けるため、上記のどちらかを使う。
