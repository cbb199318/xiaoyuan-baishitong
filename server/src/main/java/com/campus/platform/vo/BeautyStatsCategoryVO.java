package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeautyStatsCategoryVO {

    private String category;
    private Integer count;
}
