package com.zaoral.backend.controller;

import com.zaoral.backend.dto.QuotaResponse;
import com.zaoral.backend.dto.UpdatePlanRequest;
import com.zaoral.backend.dto.UpdatePlanResponse;
import com.zaoral.backend.entity.UserQuota;
import com.zaoral.backend.exception.FailureMessage;
import com.zaoral.backend.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/quota")
    @FailureMessage("ユーザー情報の取得に失敗しました")
    public QuotaResponse getQuota(@RequestHeader("X-User-Id") String userId) {
        return QuotaResponse.from(accountService.getQuota(userId));
    }

    @PutMapping("/plan")
    @FailureMessage("プランの変更に失敗しました")
    public UpdatePlanResponse updatePlan(@RequestHeader("X-User-Id") String userId,
                                         @RequestBody UpdatePlanRequest request) {
        UserQuota quota = accountService.updatePlan(userId, request.plan());
        String message = "プランを" + AccountService.displayNameOf(quota.getPlan()) + "に変更しました";
        return new UpdatePlanResponse(message, quota.getPlan());
    }

}
