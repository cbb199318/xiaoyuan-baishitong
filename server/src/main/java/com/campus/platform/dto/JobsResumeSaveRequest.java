package com.campus.platform.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobsResumeSaveRequest {

    @Size(max = 64)
    private String realName;

    @Size(max = 128)
    private String major;

    @Size(max = 64)
    private String grade;

    @Size(max = 2000)
    private String skills;

    @Size(max = 255)
    private String availableTime;

    @Size(max = 64)
    private String contactPhone;

    @Size(max = 2000)
    private String introduction;
}
