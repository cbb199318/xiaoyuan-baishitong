package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeautyStatsItemVO {

    private Long id;
    private String title;
    private String category;
    private Integer favoriteCount;
    private Integer viewCount;
    private String status;
}
