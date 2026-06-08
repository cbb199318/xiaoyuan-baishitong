package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner_conversation_message")
public class PartnerConversationMessage extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private String senderName;
    private String senderRole;
    private String type;
    private String content;
}
