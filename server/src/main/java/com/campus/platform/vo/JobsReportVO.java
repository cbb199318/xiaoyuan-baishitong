package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsReportVO {

    private Long id;
    private Long postId;
    private String postTitle;
    private String reportType;
    private String reporterName;
    private String targetName;
    private String targetRole;
    private String status;
    private String description;
    private String handleRemark;
    private String publicResult;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
}
