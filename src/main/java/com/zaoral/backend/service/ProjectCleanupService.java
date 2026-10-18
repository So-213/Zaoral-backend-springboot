package com.zaoral.backend.service;

import com.zaoral.backend.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 期限切れプロジェクトの削除。
 *
 * 置き換え元は Next.js の /api/cron/cleanup-expired-projects で、Vercel Cron から月1回呼ばれていた。
 * 業務テーブルがPrismaのスキーマから外れた時点で機能放棄されていたため、こちらで再実装した。
 * 実行契機はLinuxのcronからのHTTP呼び出し(README「期限切れプロジェクトの削除」参照)。
 *
 * ユーザー操作による削除({@link ProjectService#delete}) と分けているのは、所有者の確認も残機の扱いも
 * 持たない運用処理であり、リクエスト単位の責務とは性質が違うため。
 */
@Service
public class ProjectCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ProjectCleanupService.class);

    /**
     * expires_at を過ぎてから実際に削除するまでの猶予日数。
     *
     * プロジェクトは作成から {@code ProjectService.EXPIRES_IN_DAYS} 日で公開終了し、
     * さらにこの日数が経ってからレコードを物理削除する(= 作成から62日後)。
     * 置き換え元の挙動をそのまま引き継いでいる。
     */
    private static final int RETENTION_DAYS = 31;

    private final ProjectRepository projectRepository;

    public ProjectCleanupService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * 猶予期間を過ぎたプロジェクトを削除する。
     *
     * 削除対象が無ければ0件を返すだけで、呼び出し側から見た成否は変わらない(冪等)。
     * cronは失敗時に再実行しないため、取りこぼしは次回の実行で回収される。
     */
    @Transactional
    public Result cleanupExpired() {
        Instant threshold = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);
        int deletedCount = projectRepository.deleteByExpiresAtLessThanEqual(threshold);

        // cronのログだけでなくアプリケーションのログにも実行記録を残す
        log.info("期限切れプロジェクトを削除しました。削除件数: {} 件、対象: expires_at <= {}", deletedCount, threshold);

        return new Result(deletedCount, threshold);
    }

    /**
     * @param deletedCount 削除したプロジェクトの件数
     * @param threshold    削除の基準時刻。この時刻以前に公開終了したものを削除した
     */
    public record Result(int deletedCount, Instant threshold) {
    }

}
