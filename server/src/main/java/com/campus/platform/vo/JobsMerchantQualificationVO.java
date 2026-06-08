package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsMerchantQualificationVO {

    private Long id;
    private String applicantName;
    private String businessName;
    private String phone;
    private String identityType;
    private String area;
    private String licenseImage;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
}
