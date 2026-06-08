package com.campus.platform.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerMobileConversationVO {

    private String id;
    private Long demandId;
    private String demandTitle;
    private String demandSummary;
    private String demandType;
    private String demandLocation;
    private Long publisherId;
    private String publisherName;
    private Long applicantId;
    private String applicantName;
    private String status;
    private String latestMessage;
    private String latestMessageType;
    private Integer unreadCount;
    private LocalDateTime updatedAt;
    private List<PartnerMobileChatMessageVO> messages;
}
