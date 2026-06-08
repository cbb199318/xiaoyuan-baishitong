package com.campus.platform.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerMobileProfileVO {

    private Long userId;
    private String nickname;
    private String phone;
    private String bio;
    private List<String> tags;
    private Integer demandCount;
    private Integer activeDemandCount;
}
