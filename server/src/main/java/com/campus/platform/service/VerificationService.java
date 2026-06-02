package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.BusinessException;
import com.campus.platform.config.AppProperties;
import com.campus.platform.dto.AdminReviewVerificationRequest;
import com.campus.platform.dto.VerificationSubmitRequest;
import com.campus.platform.entity.AdminOperationLog;
import com.campus.platform.entity.FileAsset;
import com.campus.platform.entity.RealNameVerification;
import com.campus.platform.enums.VerificationStatus;
import com.campus.platform.mapper.AdminOperationLogMapper;
import com.campus.platform.mapper.FileAssetMapper;
import com.campus.platform.mapper.RealNameVerificationMapper;
import com.campus.platform.vo.VerificationVO;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final RealNameVerificationMapper verificationMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final FileAssetMapper fileAssetMapper;
    private final AppProperties appProperties;
    private final MessageService messageService;

    @Transactional
    public VerificationVO submit(Long userId, VerificationSubmitRequest request) {
        RealNameVerification verification =
            verificationMapper.selectOne(new LambdaQueryWrapper<RealNameVerification>().eq(RealNameVerification::getUserId, userId));
        if (verification == null) {
            verification = new RealNameVerification();
            verification.setUserId(userId);
        }
        verification.setRealName(request.getRealName());
        verification.setIdCardNo(request.getIdCardNo());
        verification.setFrontFileId(request.getFrontFileId());
        verification.setBackFileId(request.getBackFileId());
        verification.setStatus(VerificationStatus.PENDING.name());
        verification.setRejectReason(null);
        verification.setReviewedAt(null);
        verification.setReviewedBy(null);
        if (verification.getId() == null) {
            verificationMapper.insert(verification);
        } else {
            verificationMapper.updateById(verification);
        }
        return toVo(verification);
    }

    public VerificationVO getCurrent(Long userId) {
        RealNameVerification verification =
            verificationMapper.selectOne(new LambdaQueryWrapper<RealNameVerification>().eq(RealNameVerification::getUserId, userId));
        if (verification == null) {
            return null;
        }
        return toVo(verification);
    }

    public VerificationVO getDetail(Long id) {
        RealNameVerification verification = verificationMapper.selectById(id);
        return verification == null ? null : toVo(verification);
    }

    public Page<RealNameVerification> pagePending(int current, int size) {
        return verificationMapper.selectPage(
            Page.of(current, size),
            new LambdaQueryWrapper<RealNameVerification>().orderByDesc(RealNameVerification::getCreatedAt)
        );
    }

    @Transactional
    public void review(Long adminId, Long id, AdminReviewVerificationRequest request) {
        RealNameVerification verification = verificationMapper.selectById(id);
        if (verification == null) {
            throw new BusinessException("实名记录不存在");
        }
        if ("approve".equalsIgnoreCase(request.getAction())) {
            verification.setStatus(VerificationStatus.APPROVED.name());
            verification.setRejectReason(null);
        } else {
            verification.setStatus(VerificationStatus.REJECTED.name());
            verification.setRejectReason(request.getRejectReason());
        }
        verification.setReviewedBy(adminId);
        verification.setReviewedAt(LocalDateTime.now());
        verificationMapper.updateById(verification);

        messageService.sendSystemMessage(adminId, verification.getUserId(), "实名认证结果已更新，当前状态为：" + verification.getStatus());

        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(adminId);
        log.setOperationType("REVIEW_VERIFICATION");
        log.setTargetType("VERIFICATION");
        log.setTargetId(id);
        log.setRemark(request.getAction());
        adminOperationLogMapper.insert(log);
    }

    private VerificationVO toVo(RealNameVerification verification) {
        return VerificationVO.builder()
            .id(verification.getId())
            .realName(verification.getRealName())
            .idCardNo(verification.getIdCardNo())
            .frontFileId(verification.getFrontFileId())
            .frontFileUrl(buildFileUrl(verification.getFrontFileId()))
            .backFileId(verification.getBackFileId())
            .backFileUrl(buildFileUrl(verification.getBackFileId()))
            .status(verification.getStatus())
            .rejectReason(verification.getRejectReason())
            .reviewedBy(verification.getReviewedBy())
            .reviewedAt(verification.getReviewedAt())
            .build();
    }

    private String buildFileUrl(Long fileId) {
        if (fileId == null) {
            return null;
        }
        FileAsset asset = fileAssetMapper.selectById(fileId);
        if (asset == null) {
            return null;
        }
        return appProperties.getFile().getPublicBaseUrl() + asset.getRelativePath();
    }
}
