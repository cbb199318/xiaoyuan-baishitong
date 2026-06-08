package com.campus.platform.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrandStatsVO {

    private Long totalOrders;
    private Long publicOrders;
    private Long lockedOrders;
    private Long archivedOrders;
    private Long acceptedOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long disputedOrders;
    private Long expiredOrders;
    private BigDecimal acceptanceRate;
    private BigDecimal completionRate;
    private BigDecimal cancelRate;
    private BigDecimal grossTransactionAmount;
    private BigDecimal activeTransactionAmount;
    private BigDecimal completedTransactionAmount;
    private Long reportTotal;
    private Long processingReports;
    private Long resolvedReports;
    private Long rejectedReports;
    private List<ErrandStatsItemVO> statusBreakdown;
    private List<ErrandStatsItemVO> reportTypeBreakdown;
}
