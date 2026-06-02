package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationVO {

    private Long id;
    private String type;
    private String title;
    private Integer unreadCount;
    private MessageVO lastMessage;
    private LocalDateTime updatedAt;
}
