package com.campus.platform.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerMobileDemandVO {

    private Long id;
    private String title;
    private String type;
    private String timeText;
    private String location;
    private List<String> needTags;
    private List<String> userTags;
    private String description;
    private Integer remainingSlots;
    private Integer totalSlots;
    private String nickname;
    private Long publisherId;
    private String status;
    private Integer applyCount;
    private Integer reportCount;
    private String reviewRemark;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
