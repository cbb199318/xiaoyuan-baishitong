package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsMobilePostVO {

    private Long id;
    private String roleType;
    private Long publisherId;
    private String publisherPhone;
    private String publisherAvatarColor;
    private String category;
    private String jobType;
    private String jobTypeLabel;
    private String title;
    private String serviceMode;
    private String location;
    private String timeText;
    private String salaryText;
    private String summary;
    private String content;
    private String requirement;
    private Integer headCount;
    private Integer filledCount;
    private Boolean expired;
    private String createdAt;
    private String status;
    private Boolean publicVisible;
    private Integer favoriteCount;
    private String publisherName;
    private FileAssetVO credentialFile;
    private String qualificationRejectReason;
}
