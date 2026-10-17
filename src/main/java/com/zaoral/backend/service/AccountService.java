package com.zaoral.backend.service;

import com.zaoral.backend.entity.Plan;
import com.zaoral.backend.entity.UserQuota;
import com.zaoral.backend.entity.UserRef;
import com.zaoral.backend.exception.ApiException;
import com.zaoral.backend.repository.UserQuotaRepository;
import com.zaoral.backend.repository.UserRefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final UserRefRepository userRefRepository;
    private final UserQuotaRepository userQuotaRepository;

    public AccountService(UserRefRepository userRefRepository, UserQuotaRepository userQuotaRepository) {
        this.userRefRepository = userRefRepository;
        this.userQuotaRepository = userQuotaRepository;
    }

    @Transactional(readOnly = true)
    public UserQuota getQuota(String userId) {
        findUser(userId);
        return findQuota(userId);
    }

    /**
     * プランを変更する。バリデーション・ユーザー存在チェック・同一プランチェックを内包する。
     * 文言は現行Next.js実装(account-service.ts)に合わせている。
     */
    @Transactional
    public UserQuota updatePlan(String userId, String plan) {
        Plan newPlan;
        try {
            newPlan = Plan.valueOf(plan);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "無効なプランです。FREEまたはPREMIUMを指定してください。");
        }

        findUser(userId);
        UserQuota quota = findQuota(userId);

        if (quota.getPlan() == newPlan) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "既に" + displayNameOf(newPlan) + "プランです");
        }

        quota.setPlan(newPlan);
        return quota;
    }

    /**
     * レスポンス文言で使うプラン名。現行実装では FREE → "Free" / PREMIUM → "Premium" と表記する。
     */
    public static String displayNameOf(Plan plan) {
        return plan == Plan.FREE ? "Free" : "Premium";
    }

    public UserRef findUser(String userId) {
        return userRefRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
    }

    /**
     * quota行が無いのは "User" と user_quota の1対1が崩れた不整合であり、デフォルト値で補完せず明示的にエラーにする。
     * 補完すると不整合が隠れ、検知できなくなるため(05_DB切り分け.md 参照)。
     */
    public UserQuota findQuota(String userId) {
        return userQuotaRepository.findById(userId).orElseThrow(() -> {
            log.error("user_quota が存在しません: userId={}", userId);
            return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "ユーザーのクォータ情報が見つかりません");
        });
    }

}
