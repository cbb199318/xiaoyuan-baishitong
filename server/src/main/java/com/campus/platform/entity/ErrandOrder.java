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
@TableName("errand_order")
public class ErrandOrder extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long publisherId;
    private Long runnerId;
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
    private Integer urgentFlag;
    private Integer fragileFlag;
    private String status;
    private LocalDateTime acceptDeadline;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private Integer publicVisible;
}
