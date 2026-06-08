package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminJobsSkillSaveRequest {

    @NotBlank
    @Size(max = 64)
    private String label;

    @NotBlank
    @Size(max = 64)
    private String category;
}
