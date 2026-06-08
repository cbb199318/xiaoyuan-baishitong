package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminHandleReportRequest {

    @NotBlank
    private String status;
    private String remark;
    private String punishStatus;
    private String punishRemark;
}
