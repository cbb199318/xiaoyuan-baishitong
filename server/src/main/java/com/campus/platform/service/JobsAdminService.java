package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.common.BusinessException;
import com.campus.platform.dto.AdminJobsAccountActionRequest;
import com.campus.platform.dto.AdminJobsCreditUpdateRequest;
import com.campus.platform.dto.AdminJobsMerchantReviewRequest;
import com.campus.platform.dto.AdminJobsPostActionRequest;
import com.campus.platform.dto.AdminJobsReportActionRequest;
import com.campus.platform.dto.AdminJobsSkillSaveRequest;
import com.campus.platform.dto.AdminJobsSkillToggleRequest;
import com.campus.platform.entity.AdminOperationLog;
import com.campus.platform.entity.JobsAccount;
import com.campus.platform.entity.JobsMerchantQualification;
import com.campus.platform.entity.JobsPost;
import com.campus.platform.entity.JobsReport;
import com.campus.platform.entity.JobsSkillTag;
import com.campus.platform.mapper.AdminOperationLogMapper;
import com.campus.platform.mapper.JobsAccountMapper;
import com.campus.platform.mapper.JobsMerchantQualificationMapper;
import com.campus.platform.mapper.JobsPostMapper;
import com.campus.platform.mapper.JobsReportMapper;
import com.campus.platform.mapper.JobsSkillTagMapper;
import com.campus.platform.vo.JobsAccountVO;
import com.campus.platform.vo.JobsMerchantQualificationVO;
import com.campus.platform.vo.JobsPostVO;
import com.campus.platform.vo.JobsReportVO;
import com.campus.platform.vo.JobsSkillTagVO;
import com.campus.platform.vo.JobsStatsBucketVO;
import com.campus.platform.vo.JobsStatsHotPostVO;
import com.campus.platform.vo.JobsStatsVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
public class JobsAdminService {

    private final JobsMerchantQualificationMapper jobsMerchantQualificationMapper;
    private final JobsPostMapper jobsPostMapper;
    private final JobsAccountMapper jobsAccountMapper;
    private final JobsReportMapper jobsReportMapper;
    private final JobsSkillTagMapper jobsSkillTagMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final ObjectMapper objectMapper;

    public List<JobsMerchantQualificationVO> listMerchants() {
        return jobsMerchantQualificationMapper.selectList(
                new LambdaQueryWrapper<JobsMerchantQualification>()
                    .orderByDesc(JobsMerchantQualification::getSubmittedAt)
                    .orderByDesc(JobsMerchantQualification::getId))
            .stream()
            .map(this::toMerchantVo)
            .toList();
    }

    public JobsMerchantQualificationVO merchantDetail(Long id) {
        return toMerchantVo(getMerchant(id));
    }

    @Transactional
    public JobsMerchantQualificationVO reviewMerchant(Long adminId, Long id, AdminJobsMerchantReviewRequest request) {
        JobsMerchantQualification merchant = getMerchant(id);
        String status = normalize(request.getStatus());
        if (!List.of("APPROVED", "REJECTED", "PENDING").contains(status)) {
            throw new BusinessException("资质审核状态不合法");
        }
        merchant.setStatus(status);
        merchant.setReviewedAt("PENDING".equals(status) ? null : LocalDateTime.now());
        merchant.setReviewRemark(defaultText(request.getRemark(), defaultMerchantRemark(status)));
        jobsMerchantQualificationMapper.updateById(merchant);

        if (merchant.getAccountId() != null) {
            JobsAccount account = jobsAccountMapper.selectById(merchant.getAccountId());
            if (account != null && "EMPLOYER".equalsIgnoreCase(account.getRole())) {
                if ("APPROVED".equals(status)) {
                    account.setPublishEnabled(1);
                    account.setLatestRemark(merchant.getReviewRemark());
                } else if ("REJECTED".equals(status)) {
                    account.setPublishEnabled(0);
                    account.setLatestRemark(merchant.getReviewRemark());
                }
                jobsAccountMapper.updateById(account);
            }
        }
        insertAdminLog(adminId, "REVIEW_JOBS_MERCHANT", id, merchant.getReviewRemark());
        return toMerchantVo(merchant);
    }

    public List<JobsPostVO> listPosts() {
        return jobsPostMapper.selectList(
                new LambdaQueryWrapper<JobsPost>()
                    .orderByDesc(JobsPost::getCreatedAt)
                    .orderByDesc(JobsPost::getId))
            .stream()
            .map(this::toPostVo)
            .toList();
    }

    public JobsPostVO postDetail(Long id) {
        return toPostVo(getPost(id));
    }

    @Transactional
    public JobsPostVO updatePost(Long adminId, Long id, AdminJobsPostActionRequest request) {
        JobsPost post = getPost(id);
        String action = normalize(request.getAction());
        String remark = defaultText(request.getRemark(), defaultPostRemark(action));
        switch (action) {
            case "APPROVE" -> {
                post.setStatus("APPROVED");
                post.setPublicVisible(1);
                post.setRiskTagsJson(writeTags(removeTag(readTags(post.getRiskTagsJson()), "待核验")));
            }
            case "REJECT" -> {
                post.setStatus("REJECTED");
                post.setPublicVisible(0);
            }
            case "OFFLINE" -> {
                post.setStatus("OFFLINE");
                post.setPublicVisible(0);
            }
            case "BLOCK" -> {
                post.setStatus("BLOCKED");
                post.setPublicVisible(0);
                post.setRiskTagsJson(writeTags(addTag(readTags(post.getRiskTagsJson()), "人工拦截")));
            }
            case "RESTORE" -> {
                post.setStatus("APPROVED");
                post.setPublicVisible(1);
            }
            default -> throw new BusinessException("岗位处理动作不支持");
        }
        post.setReviewedAt(LocalDateTime.now());
        post.setReviewRemark(remark);
        jobsPostMapper.updateById(post);

        if (post.getAccountId() != null && ("BLOCK".equals(action) || "REJECT".equals(action))) {
            JobsAccount account = jobsAccountMapper.selectById(post.getAccountId());
            if (account != null) {
                account.setLatestRemark(remark);
                jobsAccountMapper.updateById(account);
            }
        }
        insertAdminLog(adminId, "UPDATE_JOBS_POST", id, remark);
        return toPostVo(post);
    }

    public List<JobsAccountVO> listAccounts() {
        return jobsAccountMapper.selectList(
                new LambdaQueryWrapper<JobsAccount>()
                    .orderByAsc(JobsAccount::getStatus)
                    .orderByAsc(JobsAccount::getCreditScore)
                    .orderByDesc(JobsAccount::getCreatedAt))
            .stream()
            .map(this::toAccountVo)
            .toList();
    }

    public JobsAccountVO accountDetail(Long id) {
        return toAccountVo(getAccount(id));
    }

    @Transactional
    public JobsAccountVO updateAccount(Long adminId, Long id, AdminJobsAccountActionRequest request) {
        JobsAccount account = getAccount(id);
        String action = normalize(request.getAction());
        String remark = defaultText(request.getRemark(), defaultAccountRemark(account, action));
        switch (action) {
            case "LIMIT" -> account.setStatus("LIMITED");
            case "BAN" -> {
                account.setStatus("BANNED");
                account.setPublishEnabled(0);
                account.setApplyEnabled(0);
                account.setAcceptEnabled(0);
            }
            case "RESTORE" -> {
                account.setStatus("ACTIVE");
                account.setPublishEnabled(1);
                account.setApplyEnabled(1);
                account.setAcceptEnabled(1);
            }
            case "TOGGLE_PUBLISH" -> account.setPublishEnabled(boolToInt(!intToBool(account.getPublishEnabled())));
            case "TOGGLE_APPLY" -> account.setApplyEnabled(boolToInt(!intToBool(account.getApplyEnabled())));
            case "TOGGLE_ACCEPT" -> account.setAcceptEnabled(boolToInt(!intToBool(account.getAcceptEnabled())));
            default -> throw new BusinessException("账号处理动作不支持");
        }
        account.setLatestRemark(remark);
        jobsAccountMapper.updateById(account);

        if ("LIMIT".equals(action) || "BAN".equals(action)) {
            offlineAccountPosts(account.getId(), "BAN".equals(action) ? "账号已封禁，岗位已下架" : "账号受限，岗位已转入人工复核");
        }
        insertAdminLog(adminId, "UPDATE_JOBS_ACCOUNT", id, remark);
        return toAccountVo(account);
    }

    @Transactional
    public JobsAccountVO updateCredit(Long adminId, Long id, AdminJobsCreditUpdateRequest request) {
        JobsAccount account = getAccount(id);
        int score = Math.max(0, Math.min(100, request.getCreditScore()));
        account.setCreditScore(score);
        account.setLatestRemark(defaultText(request.getRemark(), "管理员手动调整信用分"));
        if (score < 60 && !"BANNED".equalsIgnoreCase(account.getStatus())) {
            account.setStatus("LIMITED");
        }
        jobsAccountMapper.updateById(account);
        insertAdminLog(adminId, "UPDATE_JOBS_CREDIT", id, account.getLatestRemark());
        return toAccountVo(account);
    }

    public List<JobsReportVO> listReports() {
        return jobsReportMapper.selectList(
                new LambdaQueryWrapper<JobsReport>()
                    .orderByDesc(JobsReport::getCreatedAt)
                    .orderByDesc(JobsReport::getId))
            .stream()
            .map(this::toReportVo)
            .toList();
    }

    public JobsReportVO reportDetail(Long id) {
        return toReportVo(getReport(id));
    }

    @Transactional
    public JobsReportVO updateReport(Long adminId, Long id, AdminJobsReportActionRequest request) {
        JobsReport report = getReport(id);
        String status = normalize(request.getStatus());
        if (!List.of("PENDING", "RESOLVED", "CLOSED").contains(status)) {
            throw new BusinessException("工单状态不支持");
        }
        report.setStatus(status);
        report.setHandleRemark(defaultText(request.getRemark(), defaultReportRemark(status)));
        if (!"PENDING".equals(status)) {
            report.setHandledAt(LocalDateTime.now());
            report.setPublicResult(defaultText(request.getPublicResult(), defaultPublicResult(status)));
        }
        jobsReportMapper.updateById(report);

        if (!"PENDING".equals(status)) {
            JobsPost post = jobsPostMapper.selectById(report.getPostId());
            if (post != null && "APPROVED".equalsIgnoreCase(post.getStatus())) {
                post.setStatus("OFFLINE");
                post.setPublicVisible(0);
                post.setReviewedAt(LocalDateTime.now());
                post.setReviewRemark("关联举报处理中，岗位已先行下架复核。");
                post.setRiskTagsJson(writeTags(addTag(readTags(post.getRiskTagsJson()), "举报处理中")));
                jobsPostMapper.updateById(post);
            }
        }
        insertAdminLog(adminId, "UPDATE_JOBS_REPORT", id, report.getHandleRemark());
        return toReportVo(report);
    }

    public List<JobsSkillTagVO> listSkills() {
        return jobsSkillTagMapper.selectList(
                new LambdaQueryWrapper<JobsSkillTag>()
                    .orderByDesc(JobsSkillTag::getEnabled)
                    .orderByDesc(JobsSkillTag::getUsageCount)
                    .orderByDesc(JobsSkillTag::getUpdatedAt))
            .stream()
            .map(this::toSkillVo)
            .toList();
    }

    @Transactional
    public JobsSkillTagVO createSkill(Long adminId, AdminJobsSkillSaveRequest request) {
        JobsSkillTag skillTag = new JobsSkillTag();
        skillTag.setLabel(request.getLabel().trim());
        skillTag.setCategory(request.getCategory().trim());
        skillTag.setEnabled(1);
        skillTag.setUsageCount(0);
        jobsSkillTagMapper.insert(skillTag);
        insertAdminLog(adminId, "CREATE_JOBS_SKILL", skillTag.getId(), "新增技能标签");
        return toSkillVo(skillTag);
    }

    @Transactional
    public JobsSkillTagVO updateSkill(Long adminId, Long id, AdminJobsSkillSaveRequest request) {
        JobsSkillTag skillTag = getSkill(id);
        skillTag.setLabel(request.getLabel().trim());
        skillTag.setCategory(request.getCategory().trim());
        jobsSkillTagMapper.updateById(skillTag);
        insertAdminLog(adminId, "UPDATE_JOBS_SKILL", id, "编辑技能标签");
        return toSkillVo(skillTag);
    }

    @Transactional
    public JobsSkillTagVO toggleSkill(Long adminId, Long id, AdminJobsSkillToggleRequest request) {
        JobsSkillTag skillTag = getSkill(id);
        skillTag.setEnabled(boolToInt(Boolean.TRUE.equals(request.getEnabled())));
        jobsSkillTagMapper.updateById(skillTag);
        insertAdminLog(adminId, "TOGGLE_JOBS_SKILL", id, Boolean.TRUE.equals(request.getEnabled()) ? "启用技能标签" : "停用技能标签");
        return toSkillVo(skillTag);
    }

    public JobsStatsVO stats() {
        List<JobsPost> posts = jobsPostMapper.selectList(new LambdaQueryWrapper<JobsPost>().orderByDesc(JobsPost::getApplicantCount));
        List<JobsAccount> accounts = jobsAccountMapper.selectList(new LambdaQueryWrapper<JobsAccount>());
        List<JobsReport> reports = jobsReportMapper.selectList(new LambdaQueryWrapper<JobsReport>());
        List<JobsSkillTag> skills = jobsSkillTagMapper.selectList(new LambdaQueryWrapper<JobsSkillTag>());

        List<JobsStatsHotPostVO> hotPosts = posts.stream()
            .sorted((left, right) -> Integer.compare(nullSafe(right.getApplicantCount()), nullSafe(left.getApplicantCount())))
            .limit(5)
            .map(item -> JobsStatsHotPostVO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .count(nullSafe(item.getApplicantCount()))
                .role(item.getRole())
                .build())
            .toList();

        return JobsStatsVO.builder()
            .totalPosts(posts.size())
            .studentPosts((int) posts.stream().filter(item -> "STUDENT".equalsIgnoreCase(item.getRole())).count())
            .employerPosts((int) posts.stream().filter(item -> "EMPLOYER".equalsIgnoreCase(item.getRole())).count())
            .activeUsers((int) accounts.stream().filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus())).count())
            .totalApplicants(posts.stream().mapToInt(item -> nullSafe(item.getApplicantCount())).sum())
            .salaryDisputes((int) reports.stream().filter(item -> "薪资拖欠".equals(item.getReportType())).count())
            .hotPosts(hotPosts)
            .areaBreakdown(toBreakdown(posts.stream().map(JobsPost::getArea).toList()))
            .reportBreakdown(toBreakdown(reports.stream().map(JobsReport::getReportType).toList()))
            .skillBreakdown(skills.stream()
                .sorted((left, right) -> Integer.compare(nullSafe(right.getUsageCount()), nullSafe(left.getUsageCount())))
                .limit(6)
                .map(item -> JobsStatsBucketVO.builder().label(item.getLabel()).count(nullSafe(item.getUsageCount())).build())
                .toList())
            .build();
    }

    private JobsMerchantQualification getMerchant(Long id) {
        JobsMerchantQualification merchant = jobsMerchantQualificationMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException("资质申请不存在");
        }
        return merchant;
    }

    private JobsPost getPost(Long id) {
        JobsPost post = jobsPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("岗位不存在");
        }
        return post;
    }

    private JobsAccount getAccount(Long id) {
        JobsAccount account = jobsAccountMapper.selectById(id);
        if (account == null) {
            throw new BusinessException("账号不存在");
        }
        return account;
    }

    private JobsReport getReport(Long id) {
        JobsReport report = jobsReportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("工单不存在");
        }
        return report;
    }

    private JobsSkillTag getSkill(Long id) {
        JobsSkillTag skillTag = jobsSkillTagMapper.selectById(id);
        if (skillTag == null) {
            throw new BusinessException("技能标签不存在");
        }
        return skillTag;
    }

    private JobsMerchantQualificationVO toMerchantVo(JobsMerchantQualification item) {
        return JobsMerchantQualificationVO.builder()
            .id(item.getId())
            .applicantName(item.getApplicantName())
            .businessName(item.getBusinessName())
            .phone(item.getPhone())
            .identityType(item.getIdentityType())
            .area(item.getArea())
            .licenseImage(item.getLicenseImage())
            .status(item.getStatus())
            .submittedAt(item.getSubmittedAt())
            .reviewedAt(item.getReviewedAt())
            .reviewRemark(item.getReviewRemark())
            .build();
    }

    private JobsPostVO toPostVo(JobsPost item) {
        return JobsPostVO.builder()
            .id(item.getId())
            .role(item.getRole())
            .publisherName(item.getPublisherName())
            .publisherPhone(item.getPublisherPhone())
            .title(item.getTitle())
            .category(item.getCategory())
            .workMode(item.getWorkMode())
            .distanceRange(item.getDistanceRange())
            .actualDistanceKm(item.getActualDistanceKm() == null ? BigDecimal.ZERO : item.getActualDistanceKm())
            .area(item.getArea())
            .salary(item.getSalary() == null ? BigDecimal.ZERO : item.getSalary())
            .settlement(item.getSettlement())
            .schedule(item.getSchedule())
            .applicantCount(nullSafe(item.getApplicantCount()))
            .status(item.getStatus())
            .riskTags(readTags(item.getRiskTagsJson()))
            .description(item.getDescription())
            .requirement(item.getRequirement())
            .createdAt(item.getCreatedAt())
            .reviewedAt(item.getReviewedAt())
            .reviewRemark(item.getReviewRemark())
            .build();
    }

    private JobsAccountVO toAccountVo(JobsAccount item) {
        return JobsAccountVO.builder()
            .id(item.getId())
            .name(item.getName())
            .role(item.getRole())
            .phone(item.getPhone())
            .status(item.getStatus())
            .publishEnabled(intToBool(item.getPublishEnabled()))
            .applyEnabled(intToBool(item.getApplyEnabled()))
            .acceptEnabled(intToBool(item.getAcceptEnabled()))
            .reportCount(nullSafe(item.getReportCount()))
            .disputeCount(nullSafe(item.getDisputeCount()))
            .creditScore(nullSafe(item.getCreditScore()))
            .latestRemark(item.getLatestRemark())
            .createdAt(item.getCreatedAt())
            .build();
    }

    private JobsReportVO toReportVo(JobsReport item) {
        return JobsReportVO.builder()
            .id(item.getId())
            .postId(item.getPostId())
            .postTitle(item.getPostTitle())
            .reportType(item.getReportType())
            .reporterName(item.getReporterName())
            .targetName(item.getTargetName())
            .targetRole(item.getTargetRole())
            .status(item.getStatus())
            .description(item.getDescription())
            .handleRemark(item.getHandleRemark())
            .publicResult(item.getPublicResult())
            .createdAt(item.getCreatedAt())
            .handledAt(item.getHandledAt())
            .build();
    }

    private JobsSkillTagVO toSkillVo(JobsSkillTag item) {
        return JobsSkillTagVO.builder()
            .id(item.getId())
            .label(item.getLabel())
            .category(item.getCategory())
            .enabled(intToBool(item.getEnabled()))
            .usageCount(nullSafe(item.getUsageCount()))
            .updatedAt(item.getUpdatedAt())
            .build();
    }

    private void insertAdminLog(Long operatorId, String operationType, Long targetId, String remark) {
        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(operatorId);
        log.setOperationType(operationType);
        log.setTargetType("JOBS");
        log.setTargetId(targetId);
        log.setRemark(remark);
        adminOperationLogMapper.insert(log);
    }

    private void offlineAccountPosts(Long accountId, String remark) {
        List<JobsPost> posts = jobsPostMapper.selectList(new LambdaQueryWrapper<JobsPost>().eq(JobsPost::getAccountId, accountId));
        for (JobsPost post : posts) {
            if ("APPROVED".equalsIgnoreCase(post.getStatus())) {
                post.setStatus("OFFLINE");
                post.setPublicVisible(0);
                post.setReviewedAt(LocalDateTime.now());
                post.setReviewRemark(remark);
                jobsPostMapper.updateById(post);
            }
        }
    }

    private List<JobsStatsBucketVO> toBreakdown(List<String> labels) {
        Map<String, Integer> counter = new LinkedHashMap<>();
        for (String label : labels) {
            if (label == null || label.isBlank()) {
                continue;
            }
            counter.put(label, counter.getOrDefault(label, 0) + 1);
        }
        List<JobsStatsBucketVO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            result.add(JobsStatsBucketVO.builder().label(entry.getKey()).count(entry.getValue()).build());
        }
        return result;
    }

    private String writeTags(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags == null ? Collections.emptyList() : tags);
        } catch (Exception exception) {
            return "[]";
        }
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
        List<String> result = new ArrayList<>(tags);
        if (value != null && !value.isBlank() && !result.contains(value)) {
            result.add(value);
        }
        return result;
    }

    private List<String> removeTag(List<String> tags, String value) {
        List<String> result = new ArrayList<>(tags);
        result.removeIf(item -> Objects.equals(item, value));
        return result;
    }

    private String normalize(String value) {
        return String.valueOf(value == null ? "" : value).trim().toUpperCase(Locale.ROOT);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private boolean intToBool(Integer value) {
        return value != null && value == 1;
    }

    private int boolToInt(boolean value) {
        return value ? 1 : 0;
    }

    private int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultMerchantRemark(String status) {
        return switch (status) {
            case "APPROVED" -> "资质照片清晰，允许发布校外兼职岗位。";
            case "REJECTED" -> "资质信息不清晰，请补充营业执照照片后重提。";
            default -> "保持待审核状态";
        };
    }

    private String defaultPostRemark(String action) {
        return switch (action) {
            case "APPROVE" -> "岗位信息完整且在固定范围内，允许展示。";
            case "REJECT" -> "岗位信息存在虚假或不合规描述，已驳回。";
            case "OFFLINE" -> "岗位存在争议，先下架等待复核。";
            case "BLOCK" -> "岗位命中超范围或高风险规则，已拦截。";
            case "RESTORE" -> "岗位复核通过，恢复展示。";
            default -> "管理员已处理岗位。";
        };
    }

    private String defaultAccountRemark(JobsAccount account, String action) {
        return switch (action) {
            case "LIMIT" -> "因近期纠纷较多，临时限制账号关键操作。";
            case "BAN" -> "因多次违规，已封禁账号。";
            case "RESTORE" -> "复核后恢复账号正常权限。";
            case "TOGGLE_PUBLISH" -> intToBool(account.getPublishEnabled()) ? "关闭发岗权限，待进一步复核。" : "恢复发岗权限。";
            case "TOGGLE_APPLY" -> intToBool(account.getApplyEnabled()) ? "关闭报名权限，控制爽约风险。" : "恢复报名权限。";
            case "TOGGLE_ACCEPT" -> intToBool(account.getAcceptEnabled()) ? "关闭接单权限，待行为恢复正常。" : "恢复接单权限。";
            default -> "管理员已更新账号状态。";
        };
    }

    private String defaultReportRemark(String status) {
        return switch (status) {
            case "RESOLVED" -> "平台已介入核查，正在推动双方整改或补偿。";
            case "CLOSED" -> "平台已完成仲裁并公示结果，工单完结。";
            default -> "保持待处理状态";
        };
    }

    private String defaultPublicResult(String status) {
        return switch (status) {
            case "RESOLVED" -> "平台已介入处理，相关结果已同步至岗位与信用记录。";
            case "CLOSED" -> "工单已完结，结果保留在公示记录中。";
            default -> "";
        };
    }
}
