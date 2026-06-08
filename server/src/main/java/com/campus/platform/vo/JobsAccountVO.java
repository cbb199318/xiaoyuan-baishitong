package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsAccountVO {

    private Long id;
    private String name;
    private String role;
    private String phone;
    private String status;
    private Boolean publishEnabled;
    private Boolean applyEnabled;
    private Boolean acceptEnabled;
    private Integer reportCount;
    private Integer disputeCount;
    private Integer creditScore;
    private String latestRemark;
    private LocalDateTime createdAt;
}
