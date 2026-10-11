package com.zaoral.backend.service;

import com.zaoral.backend.dto.CreateProjectRequest;
import com.zaoral.backend.entity.Plan;
import com.zaoral.backend.entity.Project;
import com.zaoral.backend.entity.UserQuota;
import com.zaoral.backend.entity.UserRef;
import com.zaoral.backend.exception.ApiException;
import com.zaoral.backend.repository.ProjectRepository;
import com.zaoral.backend.support.Validations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    /** 有効なプロジェクトタイプ。picture型は一時停止中のため含めない(現行 config.ts と同期) */
    private static final Set<String> VALID_PROJECT_TYPES = Set.of("message");

    /** プロジェクトの有効期限(作成日時からの日数) */
    private static final int EXPIRES_IN_DAYS = 31;

    private final ProjectRepository projectRepository;
    private final AccountService accountService;

    public ProjectService(ProjectRepository projectRepository, AccountService accountService) {
        this.projectRepository = projectRepository;
        this.accountService = accountService;
    }

    @Transactional(readOnly = true)
    public List<Project> list(String userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * プロジェクトを作成する。
     * タイプ検証・残機チェック・有効期限計算・作成と残機減算のトランザクションを内包する。
     * バリデーションの順序と文言は現行Next.js実装(route.ts → project-service.ts)に合わせている。
     */
    @Transactional
    public Project create(String userId, CreateProjectRequest request) {
        Validations.requireText(request.slug(), "スラッグ");
        Validations.requireText(request.type(), "プロジェクトタイプ");
        Validations.requireText(request.name(), "プロジェクト名");

        String type = request.type();
        if (!VALID_PROJECT_TYPES.contains(type)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "無効なプロジェクトタイプです。有効なタイプ: " + String.join(", ", VALID_PROJECT_TYPES));
        }

        // タイプ固有の属性を検証する。現在は message 型のみ
        Validations.requireText(request.message(), "メッセージ");

        UserRef user = accountService.findUser(userId);
        UserQuota quota = accountService.findQuota(userId);

        boolean premium = quota.getPlan() == Plan.PREMIUM;
        if (!premium && quota.getLeftProjects() <= 0) {
            throw new ApiException(HttpStatus.FORBIDDEN, "プロジェクトの作成残機がありません。");
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Project project = new Project(
                userId,
                user.displayName(),
                type,
                request.slug(),
                request.name(),
                now,
                now.plus(EXPIRES_IN_DAYS, ChronoUnit.DAYS)
        );
        project.attachMessage(request.message());

        Project saved = projectRepository.save(project);

        // 残機を1減らす(プレミアムプランは減らさない)
        if (!premium) {
            quota.setLeftProjects(Math.max(0, quota.getLeftProjects() - 1));
        }

        return saved;
    }

    /**
     * プロジェクトを削除する。所有者でない場合は、存在しない場合と同じく404を返す。
     * 関連する projects_message / projects_picture はDBのCascadeで削除される。
     */
    @Transactional
    public void delete(String projectId, String userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "プロジェクトが見つからないか、削除権限がありません"));

        projectRepository.delete(project);
    }

}
