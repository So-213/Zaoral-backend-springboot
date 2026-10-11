package com.zaoral.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zaoral.backend.entity.Plan;
import com.zaoral.backend.entity.UserQuota;

public record QuotaResponse(
        Plan plan,
        @JsonProperty("left_projects") int leftProjects
) {

    public static QuotaResponse from(UserQuota quota) {
        return new QuotaResponse(quota.getPlan(), quota.getLeftProjects());
    }

}
