package com.campus.platform.dto;

import lombok.Data;

@Data
public class ErrandOrderSquareQuery {

    private String keyword;
    private Integer current = 1;
    private Integer size = 10;
}
