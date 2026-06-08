package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerStatsBucketVO {

    private String label;
    private Integer count;
}
