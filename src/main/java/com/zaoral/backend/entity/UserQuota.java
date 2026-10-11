package com.zaoral.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_quota")
public class UserQuota {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 16)
    private Plan plan;

    @Column(name = "left_projects", nullable = false)
    private int leftProjects;

    protected UserQuota() {
    }

    public String getUserId() {
        return userId;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public int getLeftProjects() {
        return leftProjects;
    }

    public void setLeftProjects(int leftProjects) {
        this.leftProjects = leftProjects;
    }

}
