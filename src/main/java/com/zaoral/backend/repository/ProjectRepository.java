package com.zaoral.backend.repository;

import com.zaoral.backend.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, String> {

    @EntityGraph(attributePaths = {"projectMessage", "projectPicture"})
    List<Project> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Project> findByIdAndUserId(String id, String userId);

    /**
     * expires_at が threshold 以前のプロジェクトをまとめて削除し、削除件数を返す。
     *
     * 1件ずつ読み込んで消す派生メソッド(deleteAllBy...)ではなく一括DELETEにしている。
     * 対象が月1回分まとまって現れるバッチ処理であり、エンティティを読み込む必要がないため。
     *
     * 一括DELETEはエンティティを経由しないため {@code orphanRemoval} が働かない。
     * 子テーブル(projects_message / projects_picture)はDBの ON DELETE CASCADE が追従する(V1参照)。
     * 置き換え元の {@code prisma.project.deleteMany} も同じくDBのCascadeに委ねていた。
     */
    @Modifying
    @Query("DELETE FROM Project p WHERE p.expiresAt <= :threshold")
    int deleteByExpiresAtLessThanEqual(@Param("threshold") Instant threshold);

}
