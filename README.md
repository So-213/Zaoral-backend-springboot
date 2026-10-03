# Zaoral Backend (Spring Boot)

Zaoralのフロント/バックエンド分離移行に伴う、新規バックエンドリポジトリ。

- ビルドツール: Maven
- Java: 21 (LTS)
- 現時点では**雛形のみ**(ヘルスチェックエンドポイント `/api/health` のみ実装)。ビジネスロジック・DB接続の実装はこれから。
- **ビルド・起動は未検証**(Maven未導入の環境で作成したため)。

## 移行計画ドキュメント

移行の背景・設計判断は **[Zaoral-migration-docs](https://github.com/So-213/Zaoral-migration-docs)** に集約している。

実装に着手する際は [07_バックエンド実装.md](https://github.com/So-213/Zaoral-migration-docs/blob/main/07_%E3%83%90%E3%83%83%E3%82%AF%E3%82%A8%E3%83%B3%E3%83%89%E5%AE%9F%E8%A3%85.md) の手順に従うこと。仕様に関わる変更を行った場合は、ドキュメント側も併せて更新する。

## 関連リポジトリ

| リポジトリ | 役割 |
|---|---|
| [Zaoral-frontend](https://github.com/So-213/Zaoral-frontend) | 分離後のフロントエンド(Next.js) |
| [Zaoral](https://github.com/So-213/Zaoral) | 現行の本番稼働プロジェクト(移行では変更しない) |
