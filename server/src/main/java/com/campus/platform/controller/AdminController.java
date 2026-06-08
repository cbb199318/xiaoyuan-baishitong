package com.campus.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.AdminHandleReportRequest;
import com.campus.platform.dto.AdminJobsAccountActionRequest;
import com.campus.platform.dto.AdminJobsCreditUpdateRequest;
import com.campus.platform.dto.AdminJobsMerchantReviewRequest;
import com.campus.platform.dto.AdminJobsPostActionRequest;
import com.campus.platform.dto.AdminJobsReportActionRequest;
import com.campus.platform.dto.AdminJobsSkillSaveRequest;
import com.campus.platform.dto.AdminJobsSkillToggleRequest;
import com.campus.platform.dto.AdminPartnerConversationActionRequest;
import com.campus.platform.dto.AdminPartnerDemandActionRequest;
import com.campus.platform.dto.AdminPartnerReportActionRequest;
import com.campus.platform.dto.AdminUserStatusUpdateRequest;
import com.campus.platform.dto.AdminBeautyGoodSaveRequest;
import com.campus.platform.dto.AdminBeautyAppealActionRequest;
import com.campus.platform.dto.AdminBeautyUpdateRequest;
import com.campus.platform.dto.AdminReviewVerificationRequest;
import com.campus.platform.dto.AdminSystemMessageRequest;
import com.campus.platform.dto.ErrandAdminUpdateRequest;
import com.campus.platform.dto.ErrandRuleUpdateRequest;
import com.campus.platform.dto.LoginRequest;
import com.campus.platform.service.BeautyGoodService;
import com.campus.platform.entity.RealNameVerification;
import com.campus.platform.entity.ReportTicket;
import com.campus.platform.entity.User;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.AuthService;
import com.campus.platform.service.ErrandOrderService;
import com.campus.platform.service.JobsAdminService;
import com.campus.platform.service.MessageService;
import com.campus.platform.service.PartnerAdminService;
import com.campus.platform.service.ReportService;
import com.campus.platform.service.UserService;
import com.campus.platform.service.VerificationService;
import com.campus.platform.vo.JobsAccountVO;
import com.campus.platform.vo.JobsMerchantQualificationVO;
import com.campus.platform.vo.JobsPostVO;
import com.campus.platform.vo.JobsReportVO;
import com.campus.platform.vo.JobsSkillTagVO;
import com.campus.platform.vo.JobsStatsVO;
import com.campus.platform.vo.PartnerConversationVO;
import com.campus.platform.vo.PartnerDemandVO;
import com.campus.platform.vo.PartnerReportVO;
import com.campus.platform.vo.PartnerStatsVO;
import com.campus.platform.vo.AdminUserDetailVO;
import com.campus.platform.vo.AdminErrandConversationReviewVO;
import com.campus.platform.vo.BeautyGoodVO;
import com.campus.platform.vo.BeautyAppealVO;
import com.campus.platform.vo.BeautyStatsVO;
import com.campus.platform.vo.ErrandOrderVO;
import com.campus.platform.vo.ErrandRuleVO;
import com.campus.platform.vo.ErrandStatsVO;
import com.campus.platform.vo.AuthLoginVO;
import com.campus.platform.vo.ReportVO;
import com.campus.platform.vo.VerificationVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final VerificationService verificationService;
    private final ReportService reportService;
    private final MessageService messageService;
    private final UserService userService;
    private final ErrandOrderService errandOrderService;
    private final BeautyGoodService beautyGoodService;
    private final JobsAdminService jobsAdminService;
    private final PartnerAdminService partnerAdminService;

    @PostMapping("/login")
    public ApiResponse<AuthLoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> users(@RequestParam(defaultValue = "") String keyword,
                                         @RequestParam(defaultValue = "") String role,
                                         @RequestParam(defaultValue = "") String status) {
        return ApiResponse.success(userService.listAdminUsers(keyword, role, status));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<AdminUserDetailVO> userDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(userService.getAdminDetail(id));
    }

    @PostMapping("/users/{id}/status")
    public ApiResponse<AdminUserDetailVO> updateUserStatus(@PathVariable("id") Long id,
                                                           @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        return ApiResponse.success(userService.updateStatus(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/verifications")
    public ApiResponse<Page<RealNameVerification>> verifications(@RequestParam(defaultValue = "1") int current,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(verificationService.pagePending(current, size));
    }

    @GetMapping("/verifications/{id}")
    public ApiResponse<VerificationVO> verificationDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(verificationService.getDetail(id));
    }

    @PostMapping("/verifications/{id}/review")
    public ApiResponse<Void> review(@PathVariable("id") Long id, @Valid @RequestBody AdminReviewVerificationRequest request) {
        verificationService.review(SecurityUtils.getCurrentUserId(), id, request);
        return ApiResponse.success("处理成功", null);
    }

    @GetMapping("/reports")
    public ApiResponse<Page<ReportTicket>> reports(@RequestParam(defaultValue = "1") int current,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(defaultValue = "") String module,
                                                   @RequestParam(defaultValue = "") String status,
                                                   @RequestParam(defaultValue = "") String reportType,
                                                   @RequestParam(defaultValue = "") String keyword) {
        return ApiResponse.success(reportService.pageAll(current, size, module, status, reportType, keyword));
    }

    @GetMapping("/reports/{id}")
    public ApiResponse<ReportVO> reportDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(reportService.adminDetail(id));
    }

    @GetMapping("/errand/orders")
    public ApiResponse<Page<ErrandOrderVO>> errandOrders(@RequestParam(defaultValue = "") String keyword,
                                                         @RequestParam(defaultValue = "") String status,
                                                         @RequestParam(defaultValue = "") String serviceType,
                                                         @RequestParam(defaultValue = "") String flowState,
                                                         @RequestParam(defaultValue = "") String alertType,
                                                         @RequestParam(defaultValue = "1") int current,
                                                         @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(errandOrderService.adminPage(keyword, status, serviceType, flowState, alertType, current, size));
    }

    @GetMapping("/errand/orders/{id}")
    public ApiResponse<ErrandOrderVO> errandOrderDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.adminDetail(id));
    }

    @GetMapping("/errand/orders/{id}/conversation")
    public ApiResponse<AdminErrandConversationReviewVO> errandOrderConversation(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.adminConversationReviewByOrderId(id));
    }

    @GetMapping("/errand/conversations/{id}")
    public ApiResponse<AdminErrandConversationReviewVO> errandConversation(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.adminConversationReviewByConversationId(id));
    }

    @PostMapping("/errand/orders/{id}/action")
    public ApiResponse<ErrandOrderVO> errandOrderAction(@PathVariable("id") Long id,
                                                        @Valid @RequestBody ErrandAdminUpdateRequest request) {
        return ApiResponse.success(errandOrderService.adminUpdate(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/errand/rules")
    public ApiResponse<ErrandRuleVO> errandRules() {
        return ApiResponse.success(errandOrderService.rules());
    }

    @GetMapping("/errand/stats")
    public ApiResponse<ErrandStatsVO> errandStats() {
        return ApiResponse.success(errandOrderService.adminStats());
    }

    @GetMapping("/beauty/goods")
    public ApiResponse<Page<BeautyGoodVO>> beautyGoods(@RequestParam(defaultValue = "") String keyword,
                                                       @RequestParam(defaultValue = "") String category,
                                                       @RequestParam(defaultValue = "") String status,
                                                       @RequestParam(defaultValue = "1") int current,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(beautyGoodService.adminPage(keyword, category, status, current, size));
    }

    @GetMapping("/beauty/goods/{id}")
    public ApiResponse<BeautyGoodVO> beautyGoodDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(beautyGoodService.adminDetail(id));
    }

    @PostMapping("/beauty/goods")
    public ApiResponse<BeautyGoodVO> createBeautyGood(@Valid @RequestBody AdminBeautyGoodSaveRequest request) {
        return ApiResponse.success(beautyGoodService.adminCreate(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/beauty/goods/{id}")
    public ApiResponse<BeautyGoodVO> updateBeautyGood(@PathVariable("id") Long id,
                                                      @Valid @RequestBody AdminBeautyGoodSaveRequest request) {
        return ApiResponse.success(beautyGoodService.adminUpdateContent(SecurityUtils.getCurrentUserId(), id, request));
    }

    @PostMapping("/beauty/goods/{id}/action")
    public ApiResponse<BeautyGoodVO> beautyGoodAction(@PathVariable("id") Long id,
                                                      @Valid @RequestBody AdminBeautyUpdateRequest request) {
        return ApiResponse.success(beautyGoodService.adminUpdate(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/beauty/stats")
    public ApiResponse<BeautyStatsVO> beautyStats() {
        return ApiResponse.success(beautyGoodService.stats());
    }

    @GetMapping("/beauty/appeals")
    public ApiResponse<List<BeautyAppealVO>> beautyAppeals() {
        return ApiResponse.success(beautyGoodService.listAppeals());
    }

    @GetMapping("/beauty/appeals/{id}")
    public ApiResponse<BeautyAppealVO> beautyAppealDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(beautyGoodService.appealDetail(id));
    }

    @PostMapping("/beauty/appeals/{id}/action")
    public ApiResponse<BeautyAppealVO> beautyAppealAction(@PathVariable("id") Long id,
                                                          @Valid @RequestBody AdminBeautyAppealActionRequest request) {
        return ApiResponse.success(beautyGoodService.updateAppeal(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/partner/demands")
    public ApiResponse<List<PartnerDemandVO>> partnerDemands() {
        return ApiResponse.success(partnerAdminService.listDemands());
    }

    @GetMapping("/partner/demands/{id}")
    public ApiResponse<PartnerDemandVO> partnerDemandDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(partnerAdminService.demandDetail(id));
    }

    @PostMapping("/partner/demands/{id}/action")
    public ApiResponse<PartnerDemandVO> partnerDemandAction(@PathVariable("id") Long id,
                                                            @Valid @RequestBody AdminPartnerDemandActionRequest request) {
        return ApiResponse.success(partnerAdminService.updateDemand(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/partner/reports")
    public ApiResponse<List<PartnerReportVO>> partnerReports() {
        return ApiResponse.success(partnerAdminService.listReports());
    }

    @GetMapping("/partner/reports/{id}")
    public ApiResponse<PartnerReportVO> partnerReportDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(partnerAdminService.reportDetail(id));
    }

    @PostMapping("/partner/reports/{id}/action")
    public ApiResponse<PartnerReportVO> partnerReportAction(@PathVariable("id") Long id,
                                                            @Valid @RequestBody AdminPartnerReportActionRequest request) {
        return ApiResponse.success(partnerAdminService.updateReport(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/partner/conversations")
    public ApiResponse<List<PartnerConversationVO>> partnerConversations() {
        return ApiResponse.success(partnerAdminService.listConversations());
    }

    @GetMapping("/partner/conversations/{id}")
    public ApiResponse<PartnerConversationVO> partnerConversationDetail(@PathVariable("id") String id) {
        return ApiResponse.success(partnerAdminService.conversationDetail(id));
    }

    @PostMapping("/partner/conversations/{id}/action")
    public ApiResponse<PartnerConversationVO> partnerConversationAction(@PathVariable("id") String id,
                                                                        @Valid @RequestBody AdminPartnerConversationActionRequest request) {
        return ApiResponse.success(partnerAdminService.updateConversation(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/partner/stats")
    public ApiResponse<PartnerStatsVO> partnerStats() {
        return ApiResponse.success(partnerAdminService.stats());
    }

    @GetMapping("/jobs/merchants")
    public ApiResponse<List<JobsMerchantQualificationVO>> jobsMerchants() {
        return ApiResponse.success(jobsAdminService.listMerchants());
    }

    @GetMapping("/jobs/merchants/{id}")
    public ApiResponse<JobsMerchantQualificationVO> jobsMerchantDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsAdminService.merchantDetail(id));
    }

    @PostMapping("/jobs/merchants/{id}/review")
    public ApiResponse<JobsMerchantQualificationVO> reviewJobsMerchant(@PathVariable("id") Long id,
                                                                       @Valid @RequestBody AdminJobsMerchantReviewRequest request) {
        return ApiResponse.success(jobsAdminService.reviewMerchant(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/jobs/posts")
    public ApiResponse<List<JobsPostVO>> jobsPosts() {
        return ApiResponse.success(jobsAdminService.listPosts());
    }

    @GetMapping("/jobs/posts/{id}")
    public ApiResponse<JobsPostVO> jobsPostDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsAdminService.postDetail(id));
    }

    @PostMapping("/jobs/posts/{id}/action")
    public ApiResponse<JobsPostVO> jobsPostAction(@PathVariable("id") Long id,
                                                  @Valid @RequestBody AdminJobsPostActionRequest request) {
        return ApiResponse.success(jobsAdminService.updatePost(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/jobs/accounts")
    public ApiResponse<List<JobsAccountVO>> jobsAccounts() {
        return ApiResponse.success(jobsAdminService.listAccounts());
    }

    @GetMapping("/jobs/accounts/{id}")
    public ApiResponse<JobsAccountVO> jobsAccountDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsAdminService.accountDetail(id));
    }

    @PostMapping("/jobs/accounts/{id}/action")
    public ApiResponse<JobsAccountVO> jobsAccountAction(@PathVariable("id") Long id,
                                                        @Valid @RequestBody AdminJobsAccountActionRequest request) {
        return ApiResponse.success(jobsAdminService.updateAccount(SecurityUtils.getCurrentUserId(), id, request));
    }

    @PostMapping("/jobs/accounts/{id}/credit")
    public ApiResponse<JobsAccountVO> jobsAccountCredit(@PathVariable("id") Long id,
                                                        @Valid @RequestBody AdminJobsCreditUpdateRequest request) {
        return ApiResponse.success(jobsAdminService.updateCredit(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/jobs/reports")
    public ApiResponse<List<JobsReportVO>> jobsReports() {
        return ApiResponse.success(jobsAdminService.listReports());
    }

    @GetMapping("/jobs/reports/{id}")
    public ApiResponse<JobsReportVO> jobsReportDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(jobsAdminService.reportDetail(id));
    }

    @PostMapping("/jobs/reports/{id}/action")
    public ApiResponse<JobsReportVO> jobsReportAction(@PathVariable("id") Long id,
                                                      @Valid @RequestBody AdminJobsReportActionRequest request) {
        return ApiResponse.success(jobsAdminService.updateReport(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/jobs/skills")
    public ApiResponse<List<JobsSkillTagVO>> jobsSkills() {
        return ApiResponse.success(jobsAdminService.listSkills());
    }

    @PostMapping("/jobs/skills")
    public ApiResponse<JobsSkillTagVO> createJobsSkill(@Valid @RequestBody AdminJobsSkillSaveRequest request) {
        return ApiResponse.success(jobsAdminService.createSkill(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/jobs/skills/{id}")
    public ApiResponse<JobsSkillTagVO> updateJobsSkill(@PathVariable("id") Long id,
                                                       @Valid @RequestBody AdminJobsSkillSaveRequest request) {
        return ApiResponse.success(jobsAdminService.updateSkill(SecurityUtils.getCurrentUserId(), id, request));
    }

    @PostMapping("/jobs/skills/{id}/toggle")
    public ApiResponse<JobsSkillTagVO> toggleJobsSkill(@PathVariable("id") Long id,
                                                       @Valid @RequestBody AdminJobsSkillToggleRequest request) {
        return ApiResponse.success(jobsAdminService.toggleSkill(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/jobs/stats")
    public ApiResponse<JobsStatsVO> jobsStats() {
        return ApiResponse.success(jobsAdminService.stats());
    }

    @PostMapping("/errand/rules")
    public ApiResponse<ErrandRuleVO> updateErrandRules(@Valid @RequestBody ErrandRuleUpdateRequest request) {
        return ApiResponse.success(errandOrderService.updateRules(SecurityUtils.getCurrentUserId(), request));
    }

    @PostMapping("/reports/{id}/handle")
    public ApiResponse<Void> handleReport(@PathVariable("id") Long id, @Valid @RequestBody AdminHandleReportRequest request) {
        reportService.handle(SecurityUtils.getCurrentUserId(), id, request);
        return ApiResponse.success("处理成功", null);
    }

    @PostMapping("/messages/system/send")
    public ApiResponse<Void> sendSystemMessage(@Valid @RequestBody AdminSystemMessageRequest request) {
        List<Long> userIds = request.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            userIds = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "USER"))
                .stream()
                .map(User::getId)
                .toList();
        }
        for (Long userId : userIds) {
            messageService.sendSystemMessage(SecurityUtils.getCurrentUserId(), userId, request.getContent());
        }
        return ApiResponse.success("发送成功", null);
    }
}
