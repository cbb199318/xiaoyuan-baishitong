package com.campus.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class JobsPostCreateRequest {

    @NotBlank
    @Size(max = 20)
    private String roleType;

    @NotBlank
    @Size(max = 30)
    private String jobType;

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 20)
    private String serviceMode;

    @NotBlank
    @Size(max = 64)
    private String location;

    @NotBlank
    @Size(max = 20)
    private String serviceDate;

    @NotBlank
    @Size(max = 20)
    private String serviceTime;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal salary;

    @Size(max = 500)
    private String summary;

    private Long credentialFileId;
}
