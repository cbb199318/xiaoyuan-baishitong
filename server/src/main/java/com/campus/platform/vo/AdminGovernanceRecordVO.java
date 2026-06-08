package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminGovernanceRecordVO {

    private String category;
    private String actionLabel;
    private String remark;
    private LocalDateTime createdAt;
}
