package com.campus.platform.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerConversationVO {

    private String id;
    private Long demandId;
    private String demandTitle;
    private String demandType;
    private String counterpartyName;
    private Long counterpartyUserId;
    private String counterpartyPhone;
    private String status;
    private String riskLevel;
    private List<String> baseRiskTags;
    private List<String> riskTags;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private String reviewRemark;
    private List<PartnerChatMessageVO> messages;
}
