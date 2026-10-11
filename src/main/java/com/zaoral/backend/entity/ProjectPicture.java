package com.zaoral.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * 子テーブル(picture タイプ専用属性)。picture型は停止中のため、読み取りのみで作成処理は持たない。
 */
@Entity
@Table(name = "projects_picture")
public class ProjectPicture {

    @Id
    private String projectId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    protected ProjectPicture() {
    }

    public String getProjectId() {
        return projectId;
    }

    public String getS3Key() {
        return s3Key;
    }

}
