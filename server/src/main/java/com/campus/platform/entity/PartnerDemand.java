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
@TableName("partner_demand")
public class PartnerDemand extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String type;
    private String location;
    private String schedule;
    private String publisherName;
    private Long publisherUserId;
    private String publisherPhone;
    private String description;
    private String needTagsJson;
    private String status;
    private String baseRiskTagsJson;
    private Integer applyCount;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
}
