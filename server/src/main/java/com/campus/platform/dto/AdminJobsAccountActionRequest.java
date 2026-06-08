package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminJobsAccountActionRequest {

    @NotBlank
    private String action;

    @Size(max = 255)
    private String remark;
}
