package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminReviewVerificationRequest {

    @NotBlank
    private String action;
    private String rejectReason;
}

