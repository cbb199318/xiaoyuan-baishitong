package com.campus.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ErrandRuleUpdateRequest {

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal urgentFee;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal fragileFee;

    @NotNull
    private Integer publishLimitPerUser;

    @NotNull
    private Integer acceptLimitPerUser;

    @NotNull
    private Integer autoExpireHours;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal minBaseFee;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal maxBaseFee;
}
