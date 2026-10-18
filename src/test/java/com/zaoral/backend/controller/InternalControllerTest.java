package com.zaoral.backend.controller;

import com.zaoral.backend.exception.GlobalExceptionHandler;
import com.zaoral.backend.service.ProjectCleanupService;
import com.zaoral.backend.support.CronAuthorization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 認証が実際にエンドポイントの手前で効いているかを確かめる。
 * {@link CronAuthorization} はモックせず本物を使う(認証を通す/弾くの判定がテストの対象のため)。
 */
@WebMvcTest(InternalController.class)
@Import({CronAuthorization.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "zaoral.cron.secret=test-secret")
class InternalControllerTest {

    private static final String ENDPOINT = "/api/internal/cleanup-expired-projects";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectCleanupService projectCleanupService;

    @Test
    void 正しいシークレットなら削除を実行して結果を返す() throws Exception {
        Instant threshold = Instant.parse("2026-09-16T00:00:00Z");
        when(projectCleanupService.cleanupExpired())
                .thenReturn(new ProjectCleanupService.Result(2, threshold));

        mockMvc.perform(post(ENDPOINT).header(HttpHeaders.AUTHORIZATION, "Bearer test-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted_count").value(2))
                .andExpect(jsonPath("$.threshold").value("2026-09-16T00:00:00Z"));
    }

    @Test
    void シークレットが一致しなければ削除を実行しない() throws Exception {
        mockMvc.perform(post(ENDPOINT).header(HttpHeaders.AUTHORIZATION, "Bearer wrong-secret"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("認証が必要です"));

        verify(projectCleanupService, never()).cleanupExpired();
    }

    @Test
    void Authorizationヘッダが無ければ削除を実行しない() throws Exception {
        mockMvc.perform(post(ENDPOINT))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("認証が必要です"));

        verify(projectCleanupService, never()).cleanupExpired();
    }

    // Vercel Cron 時代のGETを引き継いでいないことの確認。cronの設定ミスに気づけるようにしておく
    @Test
    void GETは受け付けない() throws Exception {
        mockMvc.perform(get(ENDPOINT).header(HttpHeaders.AUTHORIZATION, "Bearer test-secret"))
                .andExpect(status().isMethodNotAllowed());

        verify(projectCleanupService, never()).cleanupExpired();
    }

}
