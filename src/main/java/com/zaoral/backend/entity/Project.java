package com.zaoral.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 基底テーブル(共通属性)。type 固有の属性は ProjectMessage / ProjectPicture に持つ。
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    // "User" への関連は持たず、X-User-Id ヘッダの値をそのまま保持する(FKはDB側で担保)
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "type", nullable = false, length = 16)
    private String type;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProjectMessage projectMessage;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProjectPicture projectPicture;

    protected Project() {
    }

    public Project(String userId, String userName, String type, String slug, String name,
                   Instant createdAt, Instant expiresAt) {
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.slug = slug;
        this.name = name;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public void attachMessage(String message) {
        this.projectMessage = new ProjectMessage(this, message);
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getType() {
        return type;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public ProjectMessage getProjectMessage() {
        return projectMessage;
    }

    public ProjectPicture getProjectPicture() {
        return projectPicture;
    }

}
