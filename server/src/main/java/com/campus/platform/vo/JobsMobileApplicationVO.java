package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsMobileApplicationVO {

    private Long id;
    private Long jobId;
    private Long applicantId;
    private String title;
    private String location;
    private String timeText;
    private String salaryText;
    private String publisherName;
    private String publisherPhone;
    private String roleType;
    private String status;
    private String conversationId;
    private String appliedAt;
}
