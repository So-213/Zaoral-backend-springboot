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
 * 子テーブル(message タイプ専用属性)。主キーは projects.id と共有する。
 */
@Entity
@Table(name = "projects_message")
public class ProjectMessage {

    @Id
    private String projectId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "message", nullable = false)
    private String message;

    protected ProjectMessage() {
    }

    ProjectMessage(Project project, String message) {
        this.project = project;
        this.message = message;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getMessage() {
        return message;
    }

}
