package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.BusinessException;
import com.campus.platform.config.AppProperties;
import com.campus.platform.config.NotificationWebSocketHandler;
import com.campus.platform.dto.AdminHandleReportRequest;
import com.campus.platform.dto.ReportCreateRequest;
import com.campus.platform.entity.AdminOperationLog;
import com.campus.platform.entity.FileAsset;
import com.campus.platform.entity.ReportAttachment;
import com.campus.platform.entity.ReportTicket;
import com.campus.platform.enums.ReportStatus;
import com.campus.platform.mapper.AdminOperationLogMapper;
import com.campus.platform.mapper.FileAssetMapper;
import com.campus.platform.mapper.ReportAttachmentMapper;
import com.campus.platform.mapper.ReportTicketMapper;
import com.campus.platform.vo.FileAssetVO;
import com.campus.platform.vo.ReportVO;
import com.campus.platform.vo.ReportStatusChangedEventVO;
import com.campus.platform.vo.WebSocketEventVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportTicketMapper reportTicketMapper;
    private final ReportAttachmentMapper reportAttachmentMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final FileAssetMapper fileAssetMapper;
    private final AppProperties appProperties;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final ErrandOrderService errandOrderService;
    private final MessageService messageService;

    @Transactional
    public ReportVO create(Long userId, ReportCreateRequest request) {
        validateReportCreate(userId, request);

        ReportTicket report = new ReportTicket();
        report.setModule(request.getModule());
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReportType(request.getReportType());
        report.setDescription(request.getDescription());
        report.setContactPhone(request.getContactPhone());
        report.setStatus(ReportStatus.PENDING.name());
        report.setSubmitterId(userId);
        reportTicketMapper.insert(report);

        List<Long> fileIds = request.getAttachmentFileIds() == null ? Collections.emptyList() : request.getAttachmentFileIds();
        for (Long fileId : fileIds) {
            ReportAttachment attachment = new ReportAttachment();
            attachment.setReportId(report.getId());
            attachment.setFileId(fileId);
            reportAttachmentMapper.insert(attachment);
        }
        return toVo(report);
    }

    private void validateReportCreate(Long userId, ReportCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();

        List<ReportTicket> sameTargetReports = new ArrayList<>();
        if (request.getTargetId() != null) {
            sameTargetReports = reportTicketMapper.selectList(
                new LambdaQueryWrapper<ReportTicket>()
                    .eq(ReportTicket::getSubmitterId, userId)
                    .eq(ReportTicket::getModule, request.getModule())
                    .eq(ReportTicket::getTargetType, request.getTargetType())
                    .eq(ReportTicket::getTargetId, request.getTargetId())
                    .orderByDesc(ReportTicket::getCreatedAt)
            );
        }

        boolean hasOpenDuplicate = sameTargetReports.stream().anyMatch(item ->
            "PENDING".equalsIgnoreCase(item.getStatus()) || "PROCESSING".equalsIgnoreCase(item.getStatus()));
        if (hasOpenDuplicate) {
            throw new BusinessException("该内容已有处理中举报，请勿重复提交");
        }

        boolean hasRecentDuplicate = sameTargetReports.stream().anyMatch(item ->
            item.getCreatedAt() != null && item.getCreatedAt().isAfter(now.minusHours(12)));
        if (hasRecentDuplicate) {
            throw new BusinessException("同一目标 12 小时内已举报，请稍后查看处理进度");
        }

        long recentReportCount = reportTicketMapper.selectCount(
            new LambdaQueryWrapper<ReportTicket>()
                .eq(ReportTicket::getSubmitterId, userId)
                .ge(ReportTicket::getCreatedAt, now.minusMinutes(10))
        );
        if (recentReportCount >= 3) {
            throw new BusinessException("举报提交过于频繁，请 10 分钟后再试");
        }

        long rejectedCount = reportTicketMapper.selectCount(
            new LambdaQueryWrapper<ReportTicket>()
                .eq(ReportTicket::getSubmitterId, userId)
                .eq(ReportTicket::getStatus, "REJECTED")
                .ge(ReportTicket::getCreatedAt, now.minusDays(30))
        );
        if (rejectedCount >= 5) {
            throw new BusinessException("近期无效举报次数较多，举报功能已临时限制");
        }
    }

    public List<ReportVO> myReports(Long userId) {
        return reportTicketMapper.selectList(
                new LambdaQueryWrapper<ReportTicket>()
                    .eq(ReportTicket::getSubmitterId, userId)
                    .orderByDesc(ReportTicket::getCreatedAt))
            .stream()
            .map(this::toVo)
            .collect(Collectors.toList());
    }

    public ReportVO detail(Long userId, Long id) {
        ReportTicket report = reportTicketMapper.selectById(id);
        if (report == null || !report.getSubmitterId().equals(userId)) {
            return null;
        }
        return toVo(report);
    }

    public Page<ReportTicket> pageAll(int current, int size) {
        return reportTicketMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<ReportTicket>().orderByDesc(ReportTicket::getCreatedAt));
    }

    public ReportVO adminDetail(Long reportId) {
        ReportTicket report = reportTicketMapper.selectById(reportId);
        return report == null ? null : toVo(report);
    }

    @Transactional
    public void handle(Long adminId, Long reportId, AdminHandleReportRequest request) {
        ReportTicket report = reportTicketMapper.selectById(reportId);
        report.setStatus(request.getStatus());
        report.setHandleRemark(request.getRemark());
        report.setHandledBy(adminId);
        report.setHandledAt(LocalDateTime.now());
        reportTicketMapper.updateById(report);

        if ("errand".equalsIgnoreCase(report.getModule())
            && "order".equalsIgnoreCase(report.getTargetType())
            && report.getTargetId() != null
            && "PROCESSING".equalsIgnoreCase(request.getStatus())
            && errandOrderService.exists(report.getTargetId())) {
            errandOrderService.markDisputedByReport(report.getTargetId());
        }

        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(adminId);
        log.setOperationType("HANDLE_REPORT");
        log.setTargetType("REPORT");
        log.setTargetId(reportId);
        log.setRemark(request.getStatus());
        adminOperationLogMapper.insert(log);

        notificationWebSocketHandler.sendToUser(
            report.getSubmitterId(),
            new WebSocketEventVO<>("report.status.changed", ReportStatusChangedEventVO.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .handleRemark(report.getHandleRemark())
                .handledAt(report.getHandledAt())
                .build())
        );
        messageService.sendSystemMessage(adminId, report.getSubmitterId(), "举报工单状态已更新：" + report.getStatus());
    }

    private ReportVO toVo(ReportTicket report) {
        return ReportVO.builder()
            .id(report.getId())
            .module(report.getModule())
            .targetType(report.getTargetType())
            .targetId(report.getTargetId())
            .reportType(report.getReportType())
            .description(report.getDescription())
            .contactPhone(report.getContactPhone())
            .status(report.getStatus())
            .handleRemark(report.getHandleRemark())
            .attachments(listAttachments(report.getId()))
            .handledBy(report.getHandledBy())
            .handledAt(report.getHandledAt())
            .createdAt(report.getCreatedAt())
            .build();
    }

    private List<FileAssetVO> listAttachments(Long reportId) {
        return reportAttachmentMapper.selectList(new LambdaQueryWrapper<ReportAttachment>().eq(ReportAttachment::getReportId, reportId))
            .stream()
            .map(attachment -> {
                FileAsset asset = fileAssetMapper.selectById(attachment.getFileId());
                if (asset == null) {
                    return null;
                }
                return FileAssetVO.builder()
                    .fileId(asset.getId())
                    .url(appProperties.getFile().getPublicBaseUrl() + asset.getRelativePath())
                    .originalName(asset.getOriginalName())
                    .build();
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
    }
}
