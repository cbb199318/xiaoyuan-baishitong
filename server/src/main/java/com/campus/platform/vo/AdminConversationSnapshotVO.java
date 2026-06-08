package com.campus.platform.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminConversationSnapshotVO {

    private Long conversationId;
    private String conversationType;
    private String conversationTitle;
    private LocalDateTime updatedAt;
    private List<AdminConversationMemberVO> members;
    private List<AdminMessageReviewVO> messages;
}
