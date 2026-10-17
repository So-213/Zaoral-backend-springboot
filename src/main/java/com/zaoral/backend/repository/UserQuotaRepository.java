package com.zaoral.backend.repository;

import com.zaoral.backend.entity.UserQuota;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * quota行の作成はDB側のトリガ(user_quota_autocreate / V2)が "User" のINSERTと同一トランザクションで行う。
 * そのためアプリケーションから行を作る操作は持たない(05_DB切り分け.md 参照)。
 */
public interface UserQuotaRepository extends JpaRepository<UserQuota, String> {
}
