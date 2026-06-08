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
@TableName("jobs_report")
public class JobsReport extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long reporterAccountId;
    private Long targetAccountId;
    private String postTitle;
    private String reportType;
    private String reporterName;
    private String targetName;
    private String targetRole;
    private String status;
    private String description;
    private String handleRemark;
    private String publicResult;
    private LocalDateTime handledAt;
}
