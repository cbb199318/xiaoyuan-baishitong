package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.dto.AdminUserStatusUpdateRequest;
import com.campus.platform.entity.AdminOperationLog;
import com.campus.platform.entity.ErrandOrder;
import com.campus.platform.dto.ProfileUpdateRequest;
import com.campus.platform.entity.RealNameVerification;
import com.campus.platform.entity.ReportTicket;
import com.campus.platform.entity.User;
import com.campus.platform.entity.UserProfile;
import com.campus.platform.enums.ErrandOrderStatus;
import com.campus.platform.mapper.AdminOperationLogMapper;
import com.campus.platform.mapper.ErrandOrderMapper;
import com.campus.platform.mapper.ReportTicketMapper;
import com.campus.platform.mapper.RealNameVerificationMapper;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.mapper.UserProfileMapper;
import com.campus.platform.service.MessageService;
import com.campus.platform.vo.AdminGovernanceRecordVO;
import com.campus.platform.vo.AdminUserDetailVO;
import com.campus.platform.vo.UserProfileVO;
import java.time.LocalDateTime;
import java.util.List;
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
    private final ErrandOrderMapper errandOrderMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final MessageService messageService;

    public java.util.List<User> listAdminUsers(String keyword, String role, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(item -> item.like(User::getPhone, keyword).or().like(User::getNickname, keyword));
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null && !status.isBlank()) {
            if ("REPORT_RESTRICTED".equalsIgnoreCase(status)) {
                wrapper.eq(User::getReportRestricted, 1);
            } else {
                wrapper.eq(User::getStatus, status);
            }
        }
        return userMapper.selectList(wrapper);
    }

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
            .publishRole(profile == null || profile.getPublishRole() == null || profile.getPublishRole().isBlank() ? "STUDENT" : profile.getPublishRole())
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
        Long processingReportCount = reportTicketMapper.selectCount(new LambdaQueryWrapper<ReportTicket>()
            .eq(ReportTicket::getSubmitterId, userId)
            .eq(ReportTicket::getStatus, "PROCESSING"));
        Long rejectedReportCount = reportTicketMapper.selectCount(new LambdaQueryWrapper<ReportTicket>()
            .eq(ReportTicket::getSubmitterId, userId)
            .eq(ReportTicket::getStatus, "REJECTED"));
        Long publishedErrandCount = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>().eq(ErrandOrder::getPublisherId, userId));
        Long acceptedErrandCount = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>().eq(ErrandOrder::getRunnerId, userId));
        Long completedErrandCount = errandOrderMapper.selectCount(new LambdaQueryWrapper<ErrandOrder>()
            .and(wrapper -> wrapper.eq(ErrandOrder::getPublisherId, userId).or().eq(ErrandOrder::getRunnerId, userId))
            .eq(ErrandOrder::getStatus, ErrandOrderStatus.COMPLETED.name()));
        AdminOperationLog latestPunishment = adminOperationLogMapper.selectOne(new LambdaQueryWrapper<AdminOperationLog>()
            .eq(AdminOperationLog::getTargetType, "USER")
            .eq(AdminOperationLog::getTargetId, userId)
            .and(item -> item
                .like(AdminOperationLog::getOperationType, "USER_STATUS_")
                .or()
                .like(AdminOperationLog::getOperationType, "USER_REPORT_"))
            .orderByDesc(AdminOperationLog::getCreatedAt)
            .last("limit 1"));
        AdminOperationLog latestAccountGovernance = adminOperationLogMapper.selectOne(new LambdaQueryWrapper<AdminOperationLog>()
            .eq(AdminOperationLog::getTargetType, "USER")
            .eq(AdminOperationLog::getTargetId, userId)
            .like(AdminOperationLog::getOperationType, "USER_STATUS_")
            .orderByDesc(AdminOperationLog::getCreatedAt)
            .last("limit 1"));
        AdminOperationLog latestReportGovernance = adminOperationLogMapper.selectOne(new LambdaQueryWrapper<AdminOperationLog>()
            .eq(AdminOperationLog::getTargetType, "USER")
            .eq(AdminOperationLog::getTargetId, userId)
            .like(AdminOperationLog::getOperationType, "USER_REPORT_")
            .orderByDesc(AdminOperationLog::getCreatedAt)
            .last("limit 1"));
        List<AdminGovernanceRecordVO> governanceRecords = adminOperationLogMapper.selectList(new LambdaQueryWrapper<AdminOperationLog>()
                .eq(AdminOperationLog::getTargetType, "USER")
                .eq(AdminOperationLog::getTargetId, userId)
                .and(item -> item
                    .like(AdminOperationLog::getOperationType, "USER_STATUS_")
                    .or()
                    .like(AdminOperationLog::getOperationType, "USER_REPORT_"))
                .orderByDesc(AdminOperationLog::getCreatedAt)
                .last("limit 6"))
            .stream()
            .map(this::toGovernanceRecord)
            .toList();

        return AdminUserDetailVO.builder()
            .userId(user.getId())
            .phone(user.getPhone())
            .nickname(user.getNickname())
            .avatarUrl(user.getAvatarUrl())
            .role(user.getRole())
            .status(user.getStatus())
            .reportRestricted(user.getReportRestricted())
            .realNameStatus(verification == null ? "UNSUBMITTED" : verification.getStatus())
            .gender(profile == null ? null : profile.getGender())
            .contactPhone(profile == null ? null : profile.getContactPhone())
            .bio(profile == null ? null : profile.getBio())
            .reportCount(reportCount == null ? 0 : reportCount.intValue())
            .publishedErrandCount(publishedErrandCount == null ? 0 : publishedErrandCount.intValue())
            .acceptedErrandCount(acceptedErrandCount == null ? 0 : acceptedErrandCount.intValue())
            .completedErrandCount(completedErrandCount == null ? 0 : completedErrandCount.intValue())
            .processingReportCount(processingReportCount == null ? 0 : processingReportCount.intValue())
            .rejectedReportCount(rejectedReportCount == null ? 0 : rejectedReportCount.intValue())
            .latestPunishmentRemark(latestPunishment == null ? null : latestPunishment.getRemark())
            .latestAccountGovernanceRemark(latestAccountGovernance == null ? null : latestAccountGovernance.getRemark())
            .latestReportGovernanceRemark(latestReportGovernance == null ? null : latestReportGovernance.getRemark())
            .governanceRecords(governanceRecords)
            .createdAt(user.getCreatedAt())
            .build();
    }

    @Transactional
    public AdminUserDetailVO updateStatus(Long adminId, Long userId, AdminUserStatusUpdateRequest request) {
        applyAdminStatus(adminId, userId, request.getStatus(), request.getRemark());
        return getAdminDetail(userId);
    }

    public void applyAdminStatus(Long adminId, Long userId, String status, String remark) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        boolean reportPermissionAction = "REPORT_RESTRICTED".equals(normalizedStatus) || "REPORT_ACTIVE".equals(normalizedStatus);
        if (reportPermissionAction) {
            user.setReportRestricted("REPORT_RESTRICTED".equals(normalizedStatus) ? 1 : 0);
        } else {
            user.setStatus(status);
        }
        userMapper.updateById(user);

        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(adminId);
        log.setOperationType(reportPermissionAction ? "USER_REPORT_" + normalizedStatus : "USER_STATUS_" + status);
        log.setTargetType("USER");
        log.setTargetId(userId);
        log.setRemark(remark);
        adminOperationLogMapper.insert(log);

        String finalRemark = remark == null || remark.isBlank() ? "请遵守平台规则，规范使用校园服务。" : remark;
        String message = reportPermissionAction
            ? ("举报权限已更新为 " + ("REPORT_RESTRICTED".equals(normalizedStatus) ? "受限" : "正常") + "，处理说明：" + finalRemark)
            : ("账号状态已更新为 " + status + "，处理说明：" + finalRemark);
        messageService.sendSystemMessage(adminId, userId, message);
    }

    private AdminGovernanceRecordVO toGovernanceRecord(AdminOperationLog log) {
        String operationType = log.getOperationType() == null ? "" : log.getOperationType().toUpperCase();
        boolean reportRecord = operationType.startsWith("USER_REPORT_");
        String actionCode = operationType
            .replace("USER_STATUS_", "")
            .replace("USER_REPORT_", "");
        return AdminGovernanceRecordVO.builder()
            .category(reportRecord ? "举报权限" : "账号状态")
            .actionLabel(formatGovernanceActionLabel(actionCode))
            .remark(log.getRemark())
            .createdAt(log.getCreatedAt())
            .build();
    }

    private String formatGovernanceActionLabel(String actionCode) {
        return switch (actionCode) {
            case "ACTIVE" -> "恢复正常";
            case "WARNED" -> "警告";
            case "ERRAND_RESTRICTED" -> "限制跑腿";
            case "REPORT_RESTRICTED" -> "限制举报";
            case "REPORT_ACTIVE" -> "恢复举报权限";
            case "TEMP_BANNED" -> "短期封禁";
            case "PERMANENT_BANNED" -> "永久封禁";
            case "BLACKLISTED" -> "加入黑名单";
            case "DISABLED" -> "禁用";
            default -> actionCode;
        };
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
        profile.setPublishRole(normalizePublishRole(request.getPublishRole()));
        if (profile.getId() == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
        return getProfile(userId);
    }

    private String normalizePublishRole(String publishRole) {
        String normalized = publishRole == null ? "" : publishRole.trim().toUpperCase();
        return "BUSINESS".equals(normalized) ? "BUSINESS" : "STUDENT";
    }
}
