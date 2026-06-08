package com.campus.platform.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrandRuleVO {

    private BigDecimal urgentFee;
    private BigDecimal fragileFee;
    private Integer publishLimitPerUser;
    private Integer acceptLimitPerUser;
    private Integer autoExpireHours;
    private BigDecimal minBaseFee;
    private BigDecimal maxBaseFee;
}
