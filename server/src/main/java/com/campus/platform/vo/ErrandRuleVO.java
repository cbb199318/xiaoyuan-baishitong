package com.campus.platform.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrandRuleVO {

    private BigDecimal urgentFee;
    private BigDecimal fragileFee;
}
