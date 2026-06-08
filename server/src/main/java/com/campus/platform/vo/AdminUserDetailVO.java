package com.campus.platform.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserDetailVO {

    private Long userId;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private String role;
    private String status;
    private Integer reportRestricted;
    private String realNameStatus;
    private String gender;
    private String contactPhone;
    private String bio;
    private Integer reportCount;
    private Integer publishedErrandCount;
    private Integer acceptedErrandCount;
    private Integer completedErrandCount;
    private Integer processingReportCount;
    private Integer rejectedReportCount;
    private String latestPunishmentRemark;
    private String latestAccountGovernanceRemark;
    private String latestReportGovernanceRemark;
    private List<AdminGovernanceRecordVO> governanceRecords;
    private LocalDateTime createdAt;
}
