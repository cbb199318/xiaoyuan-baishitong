package com.campus.platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobsApplicationCreateRequest {

    @NotNull
    private Long postId;

    @Size(max = 64)
    private String conversationId;
}
