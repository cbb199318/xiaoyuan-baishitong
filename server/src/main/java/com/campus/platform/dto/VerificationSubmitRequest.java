package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerificationSubmitRequest {

    @NotBlank
    private String realName;

    @NotBlank
    private String idCardNo;

    @NotNull
    private Long frontFileId;

    @NotNull
    private Long backFileId;
}

