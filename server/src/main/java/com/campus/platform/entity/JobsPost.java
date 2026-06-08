package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("jobs_post")
public class JobsPost extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accountId;
    private String role;
    private String publisherName;
    private String publisherPhone;
    private String title;
    private String category;
    private String jobType;
    private String workMode;
    private String distanceRange;
    private BigDecimal actualDistanceKm;
    private String area;
    private BigDecimal salary;
    private String settlement;
    private String schedule;
    private Integer headCount;
    private Integer applicantCount;
    private String status;
    private Integer publicVisible;
    private String riskTagsJson;
    private String description;
    private String requirement;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
}
