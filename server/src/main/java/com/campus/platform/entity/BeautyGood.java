package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("beauty_good")
public class BeautyGood extends BaseEntity {

    @TableId(type = IdType.AUTO)
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
    private String skinTagsJson;
    private String reviewJson;
    private String galleryJson;
    private String imageUrl;
    private Long productImageFileId;
    private Long receiptFileId;
    private Long creatorId;
    private String creatorNickname;
    private String status;
    private String sourceType;
    private String rejectReason;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private Integer favoriteCount;
    private Integer viewCount;
    private LocalDateTime publishedAt;
}
