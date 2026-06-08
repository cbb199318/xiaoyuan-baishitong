package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner_conversation")
public class PartnerConversation extends BaseEntity {

    @TableId(type = IdType.INPUT)
    private String id;
    private Long demandId;
    private String demandTitle;
    private String demandType;
    private String counterpartyName;
    private Long counterpartyUserId;
    private String counterpartyPhone;
    private String status;
    private String riskLevel;
    private String baseRiskTagsJson;
    private String reviewRemark;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
