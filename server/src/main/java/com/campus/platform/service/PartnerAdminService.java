package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.common.BusinessException;
import com.campus.platform.dto.AdminPartnerConversationActionRequest;
import com.campus.platform.dto.AdminPartnerDemandActionRequest;
import com.campus.platform.dto.AdminPartnerReportActionRequest;
import com.campus.platform.entity.AdminOperationLog;
import com.campus.platform.entity.PartnerConversation;
import com.campus.platform.entity.PartnerConversationMessage;
import com.campus.platform.entity.PartnerDemand;
import com.campus.platform.entity.PartnerReport;
import com.campus.platform.mapper.AdminOperationLogMapper;
import com.campus.platform.mapper.PartnerConversationMapper;
import com.campus.platform.mapper.PartnerConversationMessageMapper;
import com.campus.platform.mapper.PartnerDemandMapper;
import com.campus.platform.mapper.PartnerReportMapper;
import com.campus.platform.vo.PartnerChatMessageVO;
import com.campus.platform.vo.PartnerConversationVO;
import com.campus.platform.vo.PartnerDemandVO;
import com.campus.platform.vo.PartnerHotDemandVO;
import com.campus.platform.vo.PartnerReportVO;
import com.campus.platform.vo.PartnerStatsBucketVO;
import com.campus.platform.vo.PartnerStatsVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartnerAdminService {

    private final PartnerDemandMapper partnerDemandMapper;
    private final PartnerReportMapper partnerReportMapper;
    private final PartnerConversationMapper partnerConversationMapper;
    private final PartnerConversationMessageMapper partnerConversationMessageMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final ObjectMapper objectMapper;

    public List<PartnerDemandVO> listDemands() {
        List<PartnerDemand> demands = partnerDemandMapper.selectList(
            new LambdaQueryWrapper<PartnerDemand>()
                .orderByDesc(PartnerDemand::getCreatedAt)
                .orderByDesc(PartnerDemand::getId)
        );
        Map<Long, List<PartnerReport>> reportsByDemand = groupReportsByDemand();
        Map<Long, PartnerConversation> conversationByDemand = latestConversationByDemand();
        return demands.stream()
            .map(item -> toDemandVo(item, reportsByDemand.getOrDefault(item.getId(), List.of()), conversationByDemand.get(item.getId())))
            .toList();
    }

    public PartnerDemandVO demandDetail(Long id) {
        PartnerDemand demand = getDemand(id);
        Map<Long, List<PartnerReport>> reportsByDemand = groupReportsByDemand();
        Map<Long, PartnerConversation> conversationByDemand = latestConversationByDemand();
        return toDemandVo(demand, reportsByDemand.getOrDefault(id, List.of()), conversationByDemand.get(id));
    }

    @Transactional
    public PartnerDemandVO updateDemand(Long adminId, Long id, AdminPartnerDemandActionRequest request) {
        PartnerDemand demand = getDemand(id);
        String action = normalize(request.getAction());
        String remark = defaultText(request.getRemark(), defaultDemandRemark(action));
        switch (action) {
            case "APPROVE" -> demand.setStatus("APPROVED");
            case "REJECT" -> demand.setStatus("REJECTED");
            case "OFFLINE" -> demand.setStatus("OFFLINE");
            case "RESTORE" -> demand.setStatus("APPROVED");
            default -> throw new BusinessException("找搭子需求处理动作不支持");
        }
        demand.setReviewedAt(LocalDateTime.now());
        demand.setReviewRemark(remark);
        partnerDemandMapper.updateById(demand);
        insertAdminLog(adminId, "UPDATE_PARTNER_DEMAND", id, remark);
        return demandDetail(id);
    }

    public List<PartnerReportVO> listReports() {
        return partnerReportMapper.selectList(
                new LambdaQueryWrapper<PartnerReport>()
                    .orderByDesc(PartnerReport::getCreatedAt)
                    .orderByDesc(PartnerReport::getId))
            .stream()
            .map(this::toReportVo)
            .toList();
    }

    public PartnerReportVO reportDetail(Long id) {
        return toReportVo(getReport(id));
    }

    @Transactional
    public PartnerReportVO updateReport(Long adminId, Long id, AdminPartnerReportActionRequest request) {
        PartnerReport report = getReport(id);
        String status = normalize(request.getStatus());
        if (!List.of("PENDING", "PROCESSING", "RESOLVED", "REJECTED").contains(status)) {
            throw new BusinessException("找搭子举报状态不支持");
        }
        report.setStatus(status);
        report.setHandleRemark(defaultText(request.getRemark(), defaultReportRemark(status)));
        report.setHandledAt("PENDING".equals(status) ? null : LocalDateTime.now());
        partnerReportMapper.updateById(report);

        if ("RESOLVED".equals(status)) {
            PartnerDemand demand = partnerDemandMapper.selectById(report.getTargetId());
            if (demand != null && "APPROVED".equalsIgnoreCase(demand.getStatus())) {
                demand.setStatus("OFFLINE");
                demand.setReviewedAt(LocalDateTime.now());
                demand.setReviewRemark("关联举报核实成立，已自动下架复核");
                partnerDemandMapper.updateById(demand);
            }
        }
        insertAdminLog(adminId, "UPDATE_PARTNER_REPORT", id, report.getHandleRemark());
        return reportDetail(id);
    }

    public List<PartnerConversationVO> listConversations() {
        List<PartnerConversation> conversations = partnerConversationMapper.selectList(
            new LambdaQueryWrapper<PartnerConversation>()
                .orderByDesc(PartnerConversation::getLastMessageAt)
                .orderByDesc(PartnerConversation::getCreatedAt)
        );
        Map<String, List<PartnerConversationMessage>> messagesByConversation = messagesByConversation();
        Map<Long, List<PartnerReport>> reportsByDemand = groupReportsByDemand();
        Map<String, List<PartnerReport>> reportsByConversation = groupReportsByConversation();
        return conversations.stream()
            .map(item -> toConversationVo(
                item,
                messagesByConversation.getOrDefault(item.getId(), List.of()),
                reportsByDemand.getOrDefault(item.getDemandId(), List.of()),
                reportsByConversation.getOrDefault(item.getId(), List.of())
            ))
            .toList();
    }

    public PartnerConversationVO conversationDetail(String id) {
        PartnerConversation conversation = getConversation(id);
        Map<String, List<PartnerConversationMessage>> messagesByConversation = messagesByConversation();
        Map<Long, List<PartnerReport>> reportsByDemand = groupReportsByDemand();
        Map<String, List<PartnerReport>> reportsByConversation = groupReportsByConversation();
        return toConversationVo(
            conversation,
            messagesByConversation.getOrDefault(id, List.of()),
            reportsByDemand.getOrDefault(conversation.getDemandId(), List.of()),
            reportsByConversation.getOrDefault(id, List.of())
        );
    }

    @Transactional
    public PartnerConversationVO updateConversation(Long adminId, String id, AdminPartnerConversationActionRequest request) {
        PartnerConversation conversation = getConversation(id);
        String action = normalize(request.getAction());
        String remark = defaultText(request.getRemark(), defaultConversationRemark(action));
        switch (action) {
            case "MARK_REVIEW" -> {
                conversation.setStatus("PENDING_REVIEW");
                conversation.setRiskLevel("HIGH");
            }
            case "CLOSE" -> {
                conversation.setStatus("CLOSED");
                conversation.setRiskLevel("HIGH");
            }
            case "RESTORE" -> {
                conversation.setStatus("ACTIVE");
                conversation.setRiskLevel("LOW");
            }
            default -> throw new BusinessException("找搭子会话处理动作不支持");
        }
        conversation.setReviewRemark(remark);
        partnerConversationMapper.updateById(conversation);
        insertAdminLog(adminId, "UPDATE_PARTNER_CONVERSATION", null, remark + " | " + id);
        return conversationDetail(id);
    }

    public PartnerStatsVO stats() {
        List<PartnerDemand> demands = partnerDemandMapper.selectList(new LambdaQueryWrapper<PartnerDemand>());
        List<PartnerReport> reports = partnerReportMapper.selectList(new LambdaQueryWrapper<PartnerReport>());
        List<PartnerConversation> conversations = partnerConversationMapper.selectList(new LambdaQueryWrapper<PartnerConversation>());

        List<PartnerStatsBucketVO> typeBreakdown = toBreakdown(demands.stream().map(PartnerDemand::getType).toList());
        List<PartnerStatsBucketVO> reportTypeBreakdown = toBreakdown(reports.stream().map(PartnerReport::getReportType).toList());
        List<PartnerStatsBucketVO> riskBreakdown = toBreakdown(conversations.stream().map(PartnerConversation::getRiskLevel).toList());
        List<PartnerHotDemandVO> hotDemands = demands.stream()
            .sorted((left, right) -> Integer.compare(
                nullSafe(right.getApplyCount()) + reportCountForDemand(reports, right.getId()) * 3,
                nullSafe(left.getApplyCount()) + reportCountForDemand(reports, left.getId()) * 3))
            .limit(4)
            .map(item -> PartnerHotDemandVO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .type(item.getType())
                .reportCount(reportCountForDemand(reports, item.getId()))
                .applyCount(nullSafe(item.getApplyCount()))
                .build())
            .toList();

        return PartnerStatsVO.builder()
            .totalDemands(demands.size())
            .pendingDemands((int) demands.stream().filter(item -> "PENDING".equalsIgnoreCase(item.getStatus())).count())
            .approvedDemands((int) demands.stream().filter(item -> "APPROVED".equalsIgnoreCase(item.getStatus())).count())
            .offlineDemands((int) demands.stream().filter(item -> "OFFLINE".equalsIgnoreCase(item.getStatus())).count())
            .totalReports(reports.size())
            .pendingReports((int) reports.stream().filter(item -> "PENDING".equalsIgnoreCase(item.getStatus())).count())
            .processingReports((int) reports.stream().filter(item -> "PROCESSING".equalsIgnoreCase(item.getStatus())).count())
            .activeChats((int) conversations.stream().filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus())).count())
            .reviewChats((int) conversations.stream().filter(item -> "PENDING_REVIEW".equalsIgnoreCase(item.getStatus())).count())
            .highRiskChats((int) conversations.stream().filter(item -> "HIGH".equalsIgnoreCase(item.getRiskLevel())).count())
            .totalApplications(demands.stream().mapToInt(item -> nullSafe(item.getApplyCount())).sum())
            .typeBreakdown(typeBreakdown)
            .reportTypeBreakdown(reportTypeBreakdown)
            .riskBreakdown(riskBreakdown)
            .hotDemands(hotDemands)
            .build();
    }

    private PartnerDemand getDemand(Long id) {
        PartnerDemand demand = partnerDemandMapper.selectById(id);
        if (demand == null) {
            throw new BusinessException("找搭子需求不存在");
        }
        return demand;
    }

    private PartnerReport getReport(Long id) {
        PartnerReport report = partnerReportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("找搭子举报不存在");
        }
        return report;
    }

    private PartnerConversation getConversation(String id) {
        PartnerConversation conversation = partnerConversationMapper.selectById(id);
        if (conversation == null) {
            throw new BusinessException("找搭子会话不存在");
        }
        return conversation;
    }

    private PartnerDemandVO toDemandVo(PartnerDemand demand, List<PartnerReport> reports, PartnerConversation latestConversation) {
        List<String> riskTags = new ArrayList<>(readTags(demand.getBaseRiskTagsJson()));
        if (reports.stream().anyMatch(item -> List.of("PENDING", "PROCESSING").contains(normalize(item.getStatus())))) {
            riskTags = addTag(riskTags, "举报待处理");
        }
        if (reports.stream().anyMatch(this::isReportOverdue)) {
            riskTags = addTag(riskTags, "举报已超时");
        }
        if (latestConversation != null && "HIGH".equalsIgnoreCase(latestConversation.getRiskLevel())) {
            riskTags = addTag(riskTags, "高风险会话");
        }
        if ("OFFLINE".equalsIgnoreCase(demand.getStatus())) {
            riskTags = addTag(riskTags, "内容已下架");
        }
        if ("REJECTED".equalsIgnoreCase(demand.getStatus())) {
            riskTags = addTag(riskTags, "审核已驳回");
        }
        return PartnerDemandVO.builder()
            .id(demand.getId())
            .title(demand.getTitle())
            .type(demand.getType())
            .location(demand.getLocation())
            .schedule(demand.getSchedule())
            .publisherName(demand.getPublisherName())
            .publisherUserId(demand.getPublisherUserId())
            .publisherPhone(demand.getPublisherPhone())
            .description(demand.getDescription())
            .needTags(readTags(demand.getNeedTagsJson()))
            .status(demand.getStatus())
            .baseRiskTags(readTags(demand.getBaseRiskTagsJson()))
            .riskTags(riskTags)
            .reportCount(reports.size())
            .applyCount(nullSafe(demand.getApplyCount()))
            .latestMessageAt(latestConversation == null ? null : latestConversation.getLastMessageAt())
            .createdAt(demand.getCreatedAt())
            .reviewedAt(demand.getReviewedAt())
            .reviewRemark(demand.getReviewRemark())
            .build();
    }

    private PartnerReportVO toReportVo(PartnerReport report) {
        return PartnerReportVO.builder()
            .id(report.getId())
            .module(report.getModule())
            .targetType(report.getTargetType())
            .targetId(report.getTargetId())
            .targetTitle(report.getTargetTitle())
            .reportType(report.getReportType())
            .description(report.getDescription())
            .reporterName(report.getReporterName())
            .reporterUserId(report.getReporterUserId())
            .contactPhone(report.getContactPhone())
            .status(report.getStatus())
            .conversationId(report.getConversationId())
            .handleRemark(report.getHandleRemark())
            .handledAt(report.getHandledAt())
            .createdAt(report.getCreatedAt())
            .deadlineAt(report.getDeadlineAt())
            .evidenceSummary(report.getEvidenceSummary())
            .build();
    }

    private PartnerConversationVO toConversationVo(PartnerConversation conversation,
                                                   List<PartnerConversationMessage> messages,
                                                   List<PartnerReport> demandReports,
                                                   List<PartnerReport> conversationReports) {
        List<PartnerReport> relatedReports = new ArrayList<>();
        relatedReports.addAll(demandReports);
        for (PartnerReport report : conversationReports) {
            if (relatedReports.stream().noneMatch(item -> Objects.equals(item.getId(), report.getId()))) {
                relatedReports.add(report);
            }
        }
        relatedReports.sort(Comparator.comparing(PartnerReport::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        List<String> riskTags = new ArrayList<>(readTags(conversation.getBaseRiskTagsJson()));
        if ("PENDING_REVIEW".equalsIgnoreCase(conversation.getStatus())) {
            riskTags = addTag(riskTags, "待人工复核");
        }
        if ("CLOSED".equalsIgnoreCase(conversation.getStatus())) {
            riskTags = addTag(riskTags, "会话已关闭");
        }
        if ("HIGH".equalsIgnoreCase(conversation.getRiskLevel())) {
            riskTags = addTag(riskTags, "高风险会话");
        }
        if (relatedReports.stream().anyMatch(item -> List.of("PENDING", "PROCESSING").contains(normalize(item.getStatus())))) {
            riskTags = addTag(riskTags, "关联举报处理中");
        }
        if (relatedReports.stream().anyMatch(this::isReportOverdue)) {
            riskTags = addTag(riskTags, "举报已超时");
        }
        List<PartnerChatMessageVO> messageVos = messages.stream()
            .sorted(Comparator.comparing(PartnerConversationMessage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(item -> PartnerChatMessageVO.builder()
                .id(String.valueOf(item.getId()))
                .senderName(item.getSenderName())
                .senderRole(item.getSenderRole())
                .type(item.getType())
                .content(item.getContent())
                .createdAt(item.getCreatedAt())
                .build())
            .toList();
        String lastMessage = conversation.getLastMessage();
        LocalDateTime lastMessageAt = conversation.getLastMessageAt();
        if (!messageVos.isEmpty()) {
            PartnerChatMessageVO latest = messageVos.get(messageVos.size() - 1);
            lastMessage = latest.getContent();
            lastMessageAt = latest.getCreatedAt();
        }
        return PartnerConversationVO.builder()
            .id(conversation.getId())
            .demandId(conversation.getDemandId())
            .demandTitle(conversation.getDemandTitle())
            .demandType(conversation.getDemandType())
            .counterpartyName(conversation.getCounterpartyName())
            .counterpartyUserId(conversation.getCounterpartyUserId())
            .counterpartyPhone(conversation.getCounterpartyPhone())
            .status(conversation.getStatus())
            .riskLevel(conversation.getRiskLevel())
            .baseRiskTags(readTags(conversation.getBaseRiskTagsJson()))
            .riskTags(riskTags)
            .unreadCount(nullSafe(conversation.getUnreadCount()))
            .lastMessage(defaultText(lastMessage, ""))
            .lastMessageAt(lastMessageAt)
            .createdAt(conversation.getCreatedAt())
            .reviewRemark(conversation.getReviewRemark())
            .messages(messageVos)
            .build();
    }

    private Map<Long, List<PartnerReport>> groupReportsByDemand() {
        Map<Long, List<PartnerReport>> result = new LinkedHashMap<>();
        for (PartnerReport report : partnerReportMapper.selectList(new LambdaQueryWrapper<PartnerReport>())) {
            result.computeIfAbsent(report.getTargetId(), key -> new ArrayList<>()).add(report);
        }
        return result;
    }

    private Map<String, List<PartnerReport>> groupReportsByConversation() {
        Map<String, List<PartnerReport>> result = new LinkedHashMap<>();
        for (PartnerReport report : partnerReportMapper.selectList(new LambdaQueryWrapper<PartnerReport>())) {
            if (report.getConversationId() == null || report.getConversationId().isBlank()) {
                continue;
            }
            result.computeIfAbsent(report.getConversationId(), key -> new ArrayList<>()).add(report);
        }
        return result;
    }

    private Map<Long, PartnerConversation> latestConversationByDemand() {
        Map<Long, PartnerConversation> result = new LinkedHashMap<>();
        List<PartnerConversation> list = partnerConversationMapper.selectList(new LambdaQueryWrapper<PartnerConversation>()
            .orderByDesc(PartnerConversation::getLastMessageAt)
            .orderByDesc(PartnerConversation::getCreatedAt));
        for (PartnerConversation item : list) {
            result.putIfAbsent(item.getDemandId(), item);
        }
        return result;
    }

    private Map<String, List<PartnerConversationMessage>> messagesByConversation() {
        Map<String, List<PartnerConversationMessage>> result = new LinkedHashMap<>();
        List<PartnerConversationMessage> messages = partnerConversationMessageMapper.selectList(
            new LambdaQueryWrapper<PartnerConversationMessage>()
                .orderByAsc(PartnerConversationMessage::getCreatedAt)
                .orderByAsc(PartnerConversationMessage::getId));
        for (PartnerConversationMessage item : messages) {
            result.computeIfAbsent(item.getConversationId(), key -> new ArrayList<>()).add(item);
        }
        return result;
    }

    private List<PartnerStatsBucketVO> toBreakdown(List<String> labels) {
        Map<String, Integer> counter = new LinkedHashMap<>();
        for (String label : labels) {
            if (label == null || label.isBlank()) {
                continue;
            }
            counter.put(label, counter.getOrDefault(label, 0) + 1);
        }
        List<PartnerStatsBucketVO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            result.add(PartnerStatsBucketVO.builder().label(entry.getKey()).count(entry.getValue()).build());
        }
        return result;
    }

    private int reportCountForDemand(List<PartnerReport> reports, Long demandId) {
        return (int) reports.stream().filter(item -> Objects.equals(item.getTargetId(), demandId)).count();
    }

    private boolean isReportOverdue(PartnerReport report) {
        return report.getDeadlineAt() != null && report.getDeadlineAt().isBefore(LocalDateTime.now())
            && List.of("PENDING", "PROCESSING").contains(normalize(report.getStatus()));
    }

    private List<String> readTags(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(objectMapper.readValue(json, new TypeReference<List<String>>() {
            }));
        } catch (Exception exception) {
            return new ArrayList<>();
        }
    }

    private List<String> addTag(List<String> tags, String value) {
        List<String> result = new ArrayList<>(tags == null ? Collections.emptyList() : tags);
        if (value != null && !value.isBlank() && !result.contains(value)) {
            result.add(value);
        }
        return result;
    }

    private void insertAdminLog(Long operatorId, String operationType, Long targetId, String remark) {
        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(operatorId);
        log.setOperationType(operationType);
        log.setTargetType("PARTNER");
        log.setTargetId(targetId);
        log.setRemark(remark);
        adminOperationLogMapper.insert(log);
    }

    private String normalize(String value) {
        return String.valueOf(value == null ? "" : value).trim().toUpperCase(Locale.ROOT);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultDemandRemark(String action) {
        return switch (normalize(action)) {
            case "APPROVE" -> "内容合规，允许继续展示";
            case "REJECT" -> "内容包含不适宜展示信息，已驳回";
            case "OFFLINE" -> "检测到潜在风险，先下架复核";
            case "RESTORE" -> "复核通过，恢复公开展示";
            default -> "管理员已处理找搭子需求";
        };
    }

    private String defaultReportRemark(String status) {
        return switch (normalize(status)) {
            case "PENDING" -> "待管理员处理";
            case "PROCESSING" -> "已受理举报，正在核查聊天和截图凭证";
            case "RESOLVED" -> "已核实并完成处理";
            case "REJECTED" -> "现有凭证不足，暂不支持该举报";
            default -> "管理员已更新举报状态";
        };
    }

    private String defaultConversationRemark(String action) {
        return switch (normalize(action)) {
            case "MARK_REVIEW" -> "命中高风险沟通特征，进入人工复核";
            case "CLOSE" -> "核查存在违规沟通，已关闭会话";
            case "RESTORE" -> "复核后恢复正常沟通权限";
            default -> "管理员已处理会话";
        };
    }
}
