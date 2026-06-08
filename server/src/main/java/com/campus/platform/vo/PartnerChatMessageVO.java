package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerChatMessageVO {

    private String id;
    private String senderName;
    private String senderRole;
    private String type;
    private String content;
    private LocalDateTime createdAt;
}
