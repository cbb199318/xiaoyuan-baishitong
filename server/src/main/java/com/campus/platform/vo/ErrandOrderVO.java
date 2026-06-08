package com.campus.platform.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrandOrderVO {

    private Long id;
    private String orderNo;
    private String serviceType;
    private String pickupAddress;
    private String deliveryAddress;
    private String pickupTimeText;
    private String detailContent;
    private String remark;
    private BigDecimal baseFee;
    private BigDecimal urgentFee;
    private BigDecimal fragileFee;
    private BigDecimal totalFee;
    private Boolean urgentFlag;
    private Boolean fragileFlag;
    private String status;
    private LocalDateTime acceptDeadline;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private Boolean publicVisible;
    private ErrandCounterpartyVO publisher;
    private ErrandCounterpartyVO runner;
    private List<FileAssetVO> attachments;
    private List<ReportVO> relatedReports;
    private Integer activeReportCount;
    private Integer overdueReportCount;
    private List<String> riskTags;
    private Long conversationId;
    private Boolean canAccept;
    private Boolean canCancel;
    private Boolean canStart;
    private Boolean canDeliver;
    private Boolean canComplete;
    private Boolean canReport;
    private LocalDateTime createdAt;
}
