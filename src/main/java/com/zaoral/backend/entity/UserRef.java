package com.zaoral.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * NextAuth(Prisma)所有の "User" テーブルを読み取り専用で参照する。
 * 更新・マイグレーションは行わない。必要な列のみマッピングする。
 */
@Entity
@Table(name = "\"User\"")
@Immutable
public class UserRef {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    protected UserRef() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    /**
     * プロジェクトに記録する表示名。現行Next.js実装(requireAuth)と同じく name → email → "Anonymous" の順で決める。
     */
    public String displayName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (email != null && !email.isEmpty()) {
            return email;
        }
        return "Anonymous";
    }

}
