package com.campus.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobsPostVisibilityRequest {

    @NotNull
    private Boolean publicVisible;
}
