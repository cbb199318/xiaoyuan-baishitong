package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminBeautyUpdateRequest {

    @NotBlank
    private String action;

    private String remark;
}
