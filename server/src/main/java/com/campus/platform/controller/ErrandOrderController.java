package com.campus.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.ErrandOrderCreateRequest;
import com.campus.platform.dto.ErrandOrderSquareQuery;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.ErrandOrderService;
import com.campus.platform.vo.ErrandConversationVO;
import com.campus.platform.vo.ErrandOrderVO;
import com.campus.platform.vo.ErrandRuleVO;
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
@RequestMapping("/errand")
@RequiredArgsConstructor
public class ErrandOrderController {

    private final ErrandOrderService errandOrderService;
    @GetMapping("/rules")
    public ApiResponse<ErrandRuleVO> rules() {
        return ApiResponse.success(errandOrderService.rules());
    }

    @GetMapping("/orders/square")
    public ApiResponse<Page<ErrandOrderVO>> square(@RequestParam(defaultValue = "") String keyword,
                                                   @RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        ErrandOrderSquareQuery query = new ErrandOrderSquareQuery();
        query.setKeyword(keyword);
        query.setCurrent(current);
        query.setSize(size);
        return ApiResponse.success(errandOrderService.square(query, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/orders")
    public ApiResponse<ErrandOrderVO> create(@Valid @RequestBody ErrandOrderCreateRequest request) {
        return ApiResponse.success(errandOrderService.create(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<ErrandOrderVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.detail(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/orders/{id}/accept")
    public ApiResponse<ErrandOrderVO> accept(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.accept(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/orders/{id}/start")
    public ApiResponse<ErrandOrderVO> start(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.start(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/orders/{id}/deliver")
    public ApiResponse<ErrandOrderVO> deliver(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.deliver(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/orders/{id}/complete")
    public ApiResponse<ErrandOrderVO> complete(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.complete(id, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<ErrandOrderVO> cancel(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.cancel(id, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/orders/my/published")
    public ApiResponse<List<ErrandOrderVO>> myPublished() {
        return ApiResponse.success(errandOrderService.myPublished(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/orders/my/accepted")
    public ApiResponse<List<ErrandOrderVO>> myAccepted() {
        return ApiResponse.success(errandOrderService.myAccepted(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/orders/{id}/conversation")
    public ApiResponse<ErrandConversationVO> conversation(@PathVariable("id") Long id) {
        return ApiResponse.success(errandOrderService.getConversation(id, SecurityUtils.getCurrentUserId()));
    }
}
