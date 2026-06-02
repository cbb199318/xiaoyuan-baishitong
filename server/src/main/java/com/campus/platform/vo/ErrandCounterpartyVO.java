package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrandCounterpartyVO {

    private Long userId;
    private String nickname;
    private String phone;
}
