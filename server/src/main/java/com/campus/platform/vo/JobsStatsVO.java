package com.campus.platform.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsStatsVO {

    private Integer totalPosts;
    private Integer studentPosts;
    private Integer employerPosts;
    private Integer activeUsers;
    private Integer totalApplicants;
    private Integer salaryDisputes;
    private List<JobsStatsHotPostVO> hotPosts;
    private List<JobsStatsBucketVO> areaBreakdown;
    private List<JobsStatsBucketVO> reportBreakdown;
    private List<JobsStatsBucketVO> skillBreakdown;
}
