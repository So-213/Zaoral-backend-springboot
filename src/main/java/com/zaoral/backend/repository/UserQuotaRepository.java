package com.zaoral.backend.repository;

import com.zaoral.backend.entity.UserQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserQuotaRepository extends JpaRepository<UserQuota, String> {

    /**
     * quota行を初期値(DBのDEFAULT: FREE / 残機3)で作成する。既に存在する場合は何もしない(冪等)。
     */
    @Modifying
    @Query(value = "INSERT INTO user_quota (user_id) VALUES (:userId) ON CONFLICT (user_id) DO NOTHING",
            nativeQuery = true)
    int insertIfAbsent(@Param("userId") String userId);

}
