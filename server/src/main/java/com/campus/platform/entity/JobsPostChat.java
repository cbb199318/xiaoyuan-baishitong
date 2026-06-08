package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("jobs_post_chat")
public class JobsPostChat extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long conversationId;
    private Long applicantId;
    private Long publisherId;
}
