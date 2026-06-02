package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ErrandAdminUpdateRequest {

    @NotBlank
    private String action;

    private String remark;
}
