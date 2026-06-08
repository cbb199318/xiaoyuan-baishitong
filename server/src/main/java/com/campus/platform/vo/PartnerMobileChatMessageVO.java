package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerMobileChatMessageVO {

    private String id;
    private Long senderId;
    private String senderName;
    private String type;
    private String content;
    private LocalDateTime createdAt;
}
