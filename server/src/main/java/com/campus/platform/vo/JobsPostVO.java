package com.campus.platform.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsPostVO {

    private Long id;
    private String role;
    private String publisherName;
    private String publisherPhone;
    private String title;
    private String category;
    private String workMode;
    private String distanceRange;
    private BigDecimal actualDistanceKm;
    private String area;
    private BigDecimal salary;
    private String settlement;
    private String schedule;
    private Integer applicantCount;
    private String status;
    private List<String> riskTags;
    private String description;
    private String requirement;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
}
