package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_ticket")
public class ReportTicket extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private String targetType;
    private Long targetId;
    private String reportType;
    private String description;
    private String contactPhone;
    private String status;
    private Long submitterId;
    private String handleRemark;
    private Long handledBy;
    private LocalDateTime handledAt;

    @TableField(exist = false)
    private LocalDateTime deadlineAt;

    @TableField(exist = false)
    private Boolean isOverdue;

    @TableField(exist = false)
    private Long remainingMinutes;
}
