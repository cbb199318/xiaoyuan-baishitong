package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PartnerConversationStatusRequest {

    @NotBlank
    private String status;
}
