package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationUnreadEventVO {

    private Long conversationId;
    private Integer unreadCount;
    private Integer totalUnread;
}
