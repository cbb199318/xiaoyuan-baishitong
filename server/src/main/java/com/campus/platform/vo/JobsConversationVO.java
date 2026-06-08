package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsConversationVO {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String counterpartyName;
    private String counterpartyPhone;
    private String counterpartyAvatarColor;
    private String latestMessage;
    private String latestMessageType;
    private String updatedAt;
    private Integer unreadCount;
}
