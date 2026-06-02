package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportStatusChangedEventVO {

    private Long reportId;
    private String status;
    private String handleRemark;
    private LocalDateTime handledAt;
}
