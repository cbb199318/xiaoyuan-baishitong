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
@TableName("beauty_appeal")
public class BeautyAppeal extends BaseEntity {

    @TableId(type = IdType.AUTO)
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
    private String handleRemark;
    private LocalDateTime handledAt;
}
