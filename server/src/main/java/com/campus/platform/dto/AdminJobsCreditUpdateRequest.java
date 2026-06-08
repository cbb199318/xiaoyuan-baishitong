package com.campus.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminJobsCreditUpdateRequest {

    @NotNull
    @Min(0)
    @Max(100)
    private Integer creditScore;

    @Size(max = 255)
    private String remark;
}
