package com.campus.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.platform.common.BaseEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("errand_rule_config")
public class ErrandRuleConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private BigDecimal urgentFee;
    private BigDecimal fragileFee;
    private Integer publishLimitPerUser;
    private Integer acceptLimitPerUser;
    private Integer autoExpireHours;
    private BigDecimal minBaseFee;
    private BigDecimal maxBaseFee;
}
