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
@TableName("jobs_merchant_qualification")
public class JobsMerchantQualification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accountId;
    private String applicantName;
    private String businessName;
    private String phone;
    private String identityType;
    private String area;
    private Long licenseFileId;
    private String licenseImage;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
}
