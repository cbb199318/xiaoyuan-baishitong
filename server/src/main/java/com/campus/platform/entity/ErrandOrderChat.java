package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("errand_order_chat")
public class ErrandOrderChat extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long conversationId;
}
