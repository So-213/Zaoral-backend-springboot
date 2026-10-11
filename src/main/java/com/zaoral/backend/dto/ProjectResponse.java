package com.zaoral.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zaoral.backend.entity.Project;
import com.zaoral.backend.entity.ProjectMessage;
import com.zaoral.backend.entity.ProjectPicture;

import java.time.Instant;

/**
 * プロジェクトのレスポンス。現行のPrismaの戻り値(include: projectMessage / projectPicture)と同じキー構成にする。
 * 子テーブルのキーがキャメルケース(projectId)なのも現行どおり。
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ProjectResponse(
        String id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("user_name") String userName,
        String type,
        String slug,
        String name,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("expires_at") Instant expiresAt,
        Message projectMessage,
        Picture projectPicture
) {

    public record Message(String projectId, String message) {
    }

    public record Picture(String projectId, @JsonProperty("s3_key") String s3Key) {
    }

    public static ProjectResponse from(Project project) {
        ProjectMessage message = project.getProjectMessage();
        ProjectPicture picture = project.getProjectPicture();

        return new ProjectResponse(
                project.getId(),
                project.getUserId(),
                project.getUserName(),
                project.getType(),
                project.getSlug(),
                project.getName(),
                project.getCreatedAt(),
                project.getExpiresAt(),
                message == null ? null : new Message(message.getProjectId(), message.getMessage()),
                picture == null ? null : new Picture(picture.getProjectId(), picture.getS3Key())
        );
    }

}
