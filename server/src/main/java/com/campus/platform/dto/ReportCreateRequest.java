package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class ReportCreateRequest {

    @NotBlank
    @Size(max = 30)
    private String module;

    @NotBlank
    @Size(max = 30)
    private String targetType;

    private Long targetId;

    @NotBlank
    @Size(max = 50)
    private String reportType;

    @NotBlank
    @Size(max = 500)
    private String description;

    @Size(max = 64)
    private String contactPhone;
    private List<Long> attachmentFileIds;
}
