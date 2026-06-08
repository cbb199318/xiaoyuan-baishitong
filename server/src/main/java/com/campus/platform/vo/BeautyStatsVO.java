package com.campus.platform.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeautyStatsVO {

    private Integer totalGoods;
    private Integer pendingGoods;
    private Integer approvedGoods;
    private Integer rejectedGoods;
    private Integer offlineGoods;
    private Integer totalFavorites;
    private Integer totalViews;
    private List<BeautyStatsItemVO> topViewedGoods;
    private List<BeautyStatsItemVO> topFavoritedGoods;
    private List<BeautyStatsCategoryVO> categoryBreakdown;
}
