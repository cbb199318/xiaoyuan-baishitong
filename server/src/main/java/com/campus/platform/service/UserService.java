package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.dto.ProfileUpdateRequest;
import com.campus.platform.entity.RealNameVerification;
import com.campus.platform.entity.ReportTicket;
import com.campus.platform.entity.User;
import com.campus.platform.entity.UserProfile;
import com.campus.platform.mapper.ReportTicketMapper;
import com.campus.platform.mapper.RealNameVerificationMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.mapper.UserProfileMapper;
import com.campus.platform.vo.AdminUserDetailVO;
import com.campus.platform.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final RealNameVerificationMapper verificationMapper;
    private final ReportTicketMapper reportTicketMapper;

    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        RealNameVerification verification =
            verificationMapper.selectOne(new LambdaQueryWrapper<RealNameVerification>().eq(RealNameVerification::getUserId, userId));

        return UserProfileVO.builder()
            .userId(user.getId())
            .phone(user.getPhone())
            .nickname(user.getNickname())
            .avatarUrl(user.getAvatarUrl())
            .role(user.getRole())
            .realNameStatus(verification == null ? "UNSUBMITTED" : verification.getStatus())
            .gender(profile == null ? null : profile.getGender())
            .contactPhone(profile == null ? null : profile.getContactPhone())
            .bio(profile == null ? null : profile.getBio())
            .build();
    }

    public AdminUserDetailVO getAdminDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        RealNameVerification verification =
            verificationMapper.selectOne(new LambdaQueryWrapper<RealNameVerification>().eq(RealNameVerification::getUserId, userId));
        Long reportCount = reportTicketMapper.selectCount(new LambdaQueryWrapper<ReportTicket>().eq(ReportTicket::getSubmitterId, userId));

        return AdminUserDetailVO.builder()
            .userId(user.getId())
            .phone(user.getPhone())
            .nickname(user.getNickname())
            .avatarUrl(user.getAvatarUrl())
            .role(user.getRole())
            .status(user.getStatus())
            .realNameStatus(verification == null ? "UNSUBMITTED" : verification.getStatus())
            .gender(profile == null ? null : profile.getGender())
            .contactPhone(profile == null ? null : profile.getContactPhone())
            .bio(profile == null ? null : profile.getBio())
            .reportCount(reportCount == null ? 0 : reportCount.intValue())
            .createdAt(user.getCreatedAt())
            .build();
    }

    @Transactional
    public UserProfileVO updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        userMapper.updateById(user);

        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }
        profile.setGender(request.getGender());
        profile.setContactPhone(request.getContactPhone());
        profile.setBio(request.getBio());
        if (profile.getId() == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
        return getProfile(userId);
    }
}
