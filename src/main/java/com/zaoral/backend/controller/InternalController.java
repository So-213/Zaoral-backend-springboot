package com.zaoral.backend.controller;

import com.zaoral.backend.dto.CleanupExpiredProjectsResponse;
import com.zaoral.backend.exception.FailureMessage;
import com.zaoral.backend.service.ProjectCleanupService;
import com.zaoral.backend.support.CronAuthorization;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 利用者からのリクエストではなく、運用のために叩くエンドポイント。
 * 業務APIと違って {@code X-User-Id} は受け取らず、{@link CronAuthorization} で認証する。
 */
@RestController
@RequestMapping("/api/internal")
public class InternalController {

    private final ProjectCleanupService projectCleanupService;
    private final CronAuthorization cronAuthorization;

    public InternalController(ProjectCleanupService projectCleanupService, CronAuthorization cronAuthorization) {
        this.projectCleanupService = projectCleanupService;
        this.cronAuthorization = cronAuthorization;
    }

    /**
     * 期限切れプロジェクトを削除する。Linuxのcronから月1回呼ばれる。
     *
     * 置き換え元はVercel CronのGETだったが、Vercel Cronの制約が外れたためPOSTにしている。
     * ヘッダを required = false で受けているのは、欠落時も不一致時と同じ応答に揃えるため
     * (required = true だと GlobalExceptionHandler 側の経路に分かれてしまう)。
     */
    @PostMapping("/cleanup-expired-projects")
    @FailureMessage("期限切れプロジェクトの削除に失敗しました")
    public CleanupExpiredProjectsResponse cleanupExpiredProjects(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        cronAuthorization.verify(authorization);
        return CleanupExpiredProjectsResponse.from(projectCleanupService.cleanupExpired());
    }

}
