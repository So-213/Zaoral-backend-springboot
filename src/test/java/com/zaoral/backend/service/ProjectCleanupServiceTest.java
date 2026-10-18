package com.zaoral.backend.service;

import com.zaoral.backend.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCleanupServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectCleanupService service;

    @Test
    void 猶予期間を過ぎたものを削除の対象にする() {
        when(projectRepository.deleteByExpiresAtLessThanEqual(any())).thenReturn(3);

        Instant before = Instant.now();
        ProjectCleanupService.Result result = service.cleanupExpired();
        Instant after = Instant.now();

        ArgumentCaptor<Instant> threshold = ArgumentCaptor.forClass(Instant.class);
        org.mockito.Mockito.verify(projectRepository).deleteByExpiresAtLessThanEqual(threshold.capture());

        // 基準時刻は「実行時刻の31日前」。置き換え元(expires_atから31日経過)と同じ条件であること
        assertThat(threshold.getValue())
                .isBetween(before.minus(31, ChronoUnit.DAYS), after.minus(31, ChronoUnit.DAYS));
        assertThat(result.threshold()).isEqualTo(threshold.getValue());
        assertThat(result.deletedCount()).isEqualTo(3);
    }

    @Test
    void 対象が無ければ0件を返すだけで失敗しない() {
        when(projectRepository.deleteByExpiresAtLessThanEqual(any())).thenReturn(0);

        ProjectCleanupService.Result result = service.cleanupExpired();

        assertThat(result.deletedCount()).isZero();
        assertThat(Duration.between(result.threshold(), Instant.now()).toDays()).isEqualTo(31);
    }

}
