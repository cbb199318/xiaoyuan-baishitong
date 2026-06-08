package com.campus.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PartnerConversationMessageRequest {

    @NotBlank
    @Size(max = 20)
    private String type;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
