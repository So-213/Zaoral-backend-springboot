package com.zaoral.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zaoral.backend.service.ProjectCleanupService;

import java.time.Instant;

/**
 * 期限切れプロジェクト削除の実行結果。cronのログに残る唯一の実行記録なので、件数だけでなく
 * 判定に使った基準時刻も返す(削除0件のとき、対象が無かったのか基準時刻がずれているのか区別するため)。
 */
public record CleanupExpiredProjectsResponse(
        @JsonProperty("deleted_count") int deletedCount,
        Instant threshold
) {

    public static CleanupExpiredProjectsResponse from(ProjectCleanupService.Result result) {
        return new CleanupExpiredProjectsResponse(result.deletedCount(), result.threshold());
    }

}
