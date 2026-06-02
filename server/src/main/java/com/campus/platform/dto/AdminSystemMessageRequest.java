package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class AdminSystemMessageRequest {

    private List<Long> userIds;

    @NotBlank
    private String content;
}

