package com.campus.platform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileVO {

    private Long userId;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private String role;
    private String realNameStatus;
    private String gender;
    private String contactPhone;
    private String bio;
    private String publishRole;
}
