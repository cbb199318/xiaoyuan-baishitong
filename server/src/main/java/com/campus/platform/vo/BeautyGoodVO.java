package com.campus.platform.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeautyGoodVO {

    private Long id;
    private String category;
    private String title;
    private BigDecimal price;
    private String summary;
    private String usageGuide;
    private String sceneText;
    private String evaluation;
    private String dormExperience;
    private String avoidanceGuide;
    private List<String> skinTags;
    private List<BeautyReviewVO> reviewList;
    private List<String> gallery;
    private String imageUrl;
    private FileAssetVO productImage;
    private FileAssetVO receiptImage;
    private Long creatorId;
    private String creatorNickname;
    private String status;
    private String sourceType;
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private Integer favoriteCount;
    private Integer viewCount;
    private Boolean favorite;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
