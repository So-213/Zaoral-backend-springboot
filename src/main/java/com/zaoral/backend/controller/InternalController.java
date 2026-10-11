package com.zaoral.backend.controller;

import com.zaoral.backend.dto.QuotaResponse;
import com.zaoral.backend.exception.FailureMessage;
import com.zaoral.backend.service.AccountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Next.js のサーバーサイドからのみ呼ばれる内部用API。
 */
@RestController
@RequestMapping("/api/internal")
public class InternalController {

    private final AccountService accountService;

    public InternalController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/user-quota")
    @FailureMessage("ユーザー情報の初期化に失敗しました")
    public QuotaResponse initializeUserQuota(@RequestHeader("X-User-Id") String userId) {
        return QuotaResponse.from(accountService.initializeQuota(userId));
    }

}
