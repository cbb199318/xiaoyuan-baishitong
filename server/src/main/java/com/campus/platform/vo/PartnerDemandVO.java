package com.campus.platform.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerDemandVO {

    private Long id;
    private String title;
    private String type;
    private String location;
    private String schedule;
    private String publisherName;
    private Long publisherUserId;
    private String publisherPhone;
    private String description;
    private List<String> needTags;
    private String status;
    private List<String> baseRiskTags;
    private List<String> riskTags;
    private Integer reportCount;
    private Integer applyCount;
    private LocalDateTime latestMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
}
