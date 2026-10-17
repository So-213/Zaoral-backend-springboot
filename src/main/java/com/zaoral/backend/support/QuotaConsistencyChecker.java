package com.zaoral.backend.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 起動時に "User" と user_quota の1対1が保たれているかを検査する(将来やること.md B-1)。
 *
 * 行の作成はDB側のトリガ(user_quota_autocreate / V2)が保証しており、アプリケーションに生成の責務は無い。
 * このクラスはその保証が実際に効いていることを確認するだけで、修復はしない。
 * 検知した場合は原因を調べて対処する(05_DB切り分け.md 参照)。
 *
 * 2つを別々に見ているのは役割が違うため。
 * - トリガの実在: 原因の検知。トリガが消えても孤児が現れるのは次の新規サインアップ以降なので、
 *   件数だけを見ていると実害が出るまで気づけない。
 * - 孤児の件数: 症状の検知。想定していない原因で1対1が崩れた場合も拾える。
 *
 * なおDBをリセットした場合は flyway_schema_history ごと消えるためFlywayがV2を再実行し、
 * トリガは自動的に復活する。検知対象は「履歴が残ったままトリガだけが失われた」状態。
 */
@Component
public class QuotaConsistencyChecker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QuotaConsistencyChecker.class);

    private static final String TRIGGER_NAME = "user_quota_autocreate";

    private static final String TRIGGER_COUNT_SQL = """
            SELECT count(*) FROM pg_trigger
            WHERE NOT tgisinternal AND tgname = ?
            """;

    private static final String ORPHAN_COUNT_SQL = """
            SELECT count(*) FROM "User" u
            LEFT JOIN user_quota q ON q.user_id = u.id
            WHERE q.user_id IS NULL
            """;

    private final JdbcTemplate jdbcTemplate;

    public QuotaConsistencyChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        checkTriggerExists();
        checkNoOrphans();
    }

    private void checkTriggerExists() {
        Long count = jdbcTemplate.queryForObject(TRIGGER_COUNT_SQL, Long.class, TRIGGER_NAME);
        if (count == null || count == 0) {
            log.error("quota自動生成トリガ({})が存在しません。新規ユーザーに user_quota が作られない状態です。"
                    + "手動で削除されたか \"User\" テーブルが作り直された可能性があります。V2 を適用し直してください。",
                    TRIGGER_NAME);
        } else {
            log.info("quota自動生成トリガ({})を確認しました。", TRIGGER_NAME);
        }
    }

    private void checkNoOrphans() {
        Long orphans = jdbcTemplate.queryForObject(ORPHAN_COUNT_SQL, Long.class);
        if (orphans != null && orphans > 0) {
            log.error("\"User\" はあるが user_quota が無いユーザーが {} 件あります。"
                    + "該当ユーザーは残機・プラン関連の機能が使えません。", orphans);
        } else {
            log.info("\"User\" と user_quota の整合性を確認しました(不整合 0 件)。");
        }
    }

}
