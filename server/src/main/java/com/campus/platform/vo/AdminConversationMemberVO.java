package com.campus.platform.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminConversationMemberVO {

    private Long userId;
    private String nickname;
    private String phone;
    private String avatarUrl;
    private String role;
    private String status;
    private Integer unreadCount;
    private LocalDateTime joinedAt;
}
