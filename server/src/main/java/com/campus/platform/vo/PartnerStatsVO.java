package com.campus.platform.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerStatsVO {

    private Integer totalDemands;
    private Integer pendingDemands;
    private Integer approvedDemands;
    private Integer offlineDemands;
    private Integer totalReports;
    private Integer pendingReports;
    private Integer processingReports;
    private Integer activeChats;
    private Integer reviewChats;
    private Integer highRiskChats;
    private Integer totalApplications;
    private List<PartnerStatsBucketVO> typeBreakdown;
    private List<PartnerStatsBucketVO> reportTypeBreakdown;
    private List<PartnerStatsBucketVO> riskBreakdown;
    private List<PartnerHotDemandVO> hotDemands;
}
