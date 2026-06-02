package com.campus.platform.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportVO {

    private Long id;
    private String module;
    private String targetType;
    private Long targetId;
    private String reportType;
    private String description;
    private String contactPhone;
    private String status;
    private String handleRemark;
    private List<FileAssetVO> attachments;
    private Long handledBy;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
