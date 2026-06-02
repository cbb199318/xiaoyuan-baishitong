package com.campus.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ErrandOrderCreateRequest {

    @NotBlank
    private String serviceType;

    @NotBlank
    @Size(max = 255)
    private String pickupAddress;

    @NotBlank
    @Size(max = 255)
    private String deliveryAddress;

    @Size(max = 100)
    private String pickupTimeText;

    @NotBlank
    @Size(max = 500)
    private String detailContent;

    @Size(max = 500)
    private String remark;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal baseFee;

    @NotNull
    private Boolean urgentFlag;

    @NotNull
    private Boolean fragileFlag;

    @NotNull
    private List<Long> attachmentFileIds;
}
