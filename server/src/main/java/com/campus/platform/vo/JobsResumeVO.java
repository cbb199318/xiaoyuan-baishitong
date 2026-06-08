package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobsResumeVO {

    private String realName;
    private String major;
    private String grade;
    private String skills;
    private String availableTime;
    private String contactPhone;
    private String introduction;
    private String updatedAt;
}
