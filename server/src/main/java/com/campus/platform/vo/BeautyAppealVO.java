package com.campus.platform.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeautyAppealVO {

    private Long id;
    private Long goodId;
    private String goodTitle;
    private String category;
    private String issueType;
    private String reason;
    private String buyerName;
    private String sellerName;
    private String contactPhone;
    private String status;
    private String proofSummary;
    private BigDecimal refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
    private String handleRemark;
}
