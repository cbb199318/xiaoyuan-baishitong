package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageNewEventVO {

    private Long conversationId;
    private MessageVO message;
}
