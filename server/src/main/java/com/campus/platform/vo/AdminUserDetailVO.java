package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserDetailVO {

    private Long userId;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private String role;
    private String status;
    private String realNameStatus;
    private String gender;
    private String contactPhone;
    private String bio;
    private Integer reportCount;
    private LocalDateTime createdAt;
}
