package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminMessageReviewVO {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderNickname;
    private String senderAvatarUrl;
    private String senderRole;
    private String type;
    private String content;
    private LocalDateTime createdAt;
}
