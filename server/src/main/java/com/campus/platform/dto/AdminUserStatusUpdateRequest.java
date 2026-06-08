package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserStatusUpdateRequest {

    @NotBlank
    private String status;

    private String remark;
}
