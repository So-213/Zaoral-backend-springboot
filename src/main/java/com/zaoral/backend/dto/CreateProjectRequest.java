package com.zaoral.backend.dto;

/**
 * プロジェクト作成のリクエスト。
 * message はタイプ固有の属性で、type = "message" のときのみ使う。
 */
public record CreateProjectRequest(
        String slug,
        String type,
        String name,
        String message
) {
}
