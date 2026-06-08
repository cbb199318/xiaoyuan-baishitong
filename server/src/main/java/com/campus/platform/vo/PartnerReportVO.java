package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerReportVO {

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
    private LocalDateTime createdAt;
    private LocalDateTime deadlineAt;
    private String evidenceSummary;
}
