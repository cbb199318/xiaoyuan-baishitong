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
@TableName("partner_report")
public class PartnerReport extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private String targetType;
    private Long targetId;
    private String targetTitle;
    private String reportType;
    private String description;
    private String reporterName;
    private Long reporterUserId;
    private String contactPhone;
    private String status;
    private String conversationId;
    private String handleRemark;
    private LocalDateTime handledAt;
    private LocalDateTime deadlineAt;
    private String evidenceSummary;
}
