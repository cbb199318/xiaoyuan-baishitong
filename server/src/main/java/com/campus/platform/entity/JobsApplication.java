package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("jobs_application")
public class JobsApplication extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long applicantId;
    private Long publisherId;
    private String title;
    private String location;
    private String timeText;
    private String salaryText;
    private String publisherName;
    private String publisherPhone;
    private String roleType;
    private String status;
    private String conversationId;
}
