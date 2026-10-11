package com.zaoral.backend.dto;

import com.zaoral.backend.entity.Plan;

public record UpdatePlanResponse(String message, Plan plan) {
}
