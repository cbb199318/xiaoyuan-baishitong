package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsReviewProgressVO {

    private String qualificationStatus;
    private String qualificationRejectReason;
    private String lastSubmittedAt;
    private Long latestJobId;
}
