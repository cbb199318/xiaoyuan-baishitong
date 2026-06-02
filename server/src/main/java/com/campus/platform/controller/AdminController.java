package com.campus.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.AdminHandleReportRequest;
import com.campus.platform.dto.AdminReviewVerificationRequest;
import com.campus.platform.dto.AdminSystemMessageRequest;
import com.campus.platform.dto.ErrandAdminUpdateRequest;
import com.campus.platform.dto.ErrandRuleUpdateRequest;
import com.campus.platform.dto.LoginRequest;
import com.campus.platform.entity.RealNameVerification;
import com.campus.platform.entity.ReportTicket;
import com.campus.platform.entity.User;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.AuthService;
import com.campus.platform.service.ErrandOrderService;
import com.campus.platform.service.MessageService;
import com.campus.platform.service.ReportService;
import com.campus.platform.service.UserService;
import com.campus.platform.service.VerificationService;
import com.campus.platform.vo.AdminUserDetailVO;
import com.campus.platform.vo.ErrandOrderVO;
import com.campus.platform.vo.ErrandRuleVO;
import com.campus.platform.vo.AuthLoginVO;
import com.campus.platform.vo.ReportVO;
import com.campus.platform.vo.VerificationVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/login")
    public ApiResponse<AuthLoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> users() {
        return ApiResponse.success(userMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt)));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<AdminUserDetailVO> userDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(userService.getAdminDetail(id));
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
                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(reportService.pageAll(current, size));
    }

    @GetMapping("/reports/{id}")
    public ApiResponse<ReportVO> reportDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(reportService.adminDetail(id));
    }

    @GetMapping("/errand/orders")
    public ApiResponse<Page<ErrandOrderVO>> errandOrders(@RequestParam(defaultValue = "") String keyword,
                                                         @RequestParam(defaultValue = "") String status,
                                                         @RequestParam(defaultValue = "1") int current,
                                                         @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(errandOrderService.adminPage(keyword, status, current, size));
    }

    @GetMapping("/errand/orders/{id}")
    public ApiResponse<ErrandOrderVO> errandOrderDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.adminDetail(id));
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

    @PostMapping("/errand/rules")
    public ApiResponse<ErrandRuleVO> updateErrandRules(@Valid @RequestBody ErrandRuleUpdateRequest request) {
        return ApiResponse.success(errandOrderService.updateRules(request));
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
