package com.campus.platform.controller;

import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.PartnerConversationMessageRequest;
import com.campus.platform.dto.PartnerConversationStatusRequest;
import com.campus.platform.dto.PartnerDemandSaveRequest;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.PartnerService;
import com.campus.platform.vo.PartnerMobileConversationVO;
import com.campus.platform.vo.PartnerMobileDemandVO;
import com.campus.platform.vo.PartnerMobileProfileVO;
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
@RequestMapping("/partner")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping("/demands")
    public ApiResponse<List<PartnerMobileDemandVO>> demands(@RequestParam(defaultValue = "") String keyword,
                                                            @RequestParam(defaultValue = "") String type,
                                                            @RequestParam(required = false) Long publisherId) {
        return ApiResponse.success(partnerService.listVisibleDemands(keyword, type, publisherId));
    }

    @GetMapping("/demands/{id}")
    public ApiResponse<PartnerMobileDemandVO> demandDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(partnerService.detail(id, SecurityUtils.getCurrentUserIdOrNull()));
    }

    @GetMapping("/demands/mine")
    public ApiResponse<List<PartnerMobileDemandVO>> myDemands() {
        return ApiResponse.success(partnerService.myDemands(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/conversations")
    public ApiResponse<List<PartnerMobileConversationVO>> conversations() {
        return ApiResponse.success(partnerService.conversations(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/conversations/{id}")
    public ApiResponse<PartnerMobileConversationVO> conversationDetail(@PathVariable("id") String id) {
        return ApiResponse.success(partnerService.conversationDetail(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/demands")
    public ApiResponse<PartnerMobileDemandVO> createDemand(@Valid @RequestBody PartnerDemandSaveRequest request) {
        return ApiResponse.success(partnerService.createDemand(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/demands/{id}")
    public ApiResponse<PartnerMobileDemandVO> updateDemand(@PathVariable("id") Long id,
                                                           @Valid @RequestBody PartnerDemandSaveRequest request) {
        return ApiResponse.success(partnerService.updateDemand(SecurityUtils.getCurrentUserId(), id, request));
    }

    @PostMapping("/demands/{id}/offline")
    public ApiResponse<PartnerMobileDemandVO> offlineDemand(@PathVariable("id") Long id) {
        return ApiResponse.success(partnerService.offlineDemand(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/demands/{id}/apply")
    public ApiResponse<PartnerMobileConversationVO> applyDemand(@PathVariable("id") Long id) {
        return ApiResponse.success(partnerService.apply(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/conversations/{id}/messages")
    public ApiResponse<PartnerMobileConversationVO> sendMessage(@PathVariable("id") String id,
                                                                @Valid @RequestBody PartnerConversationMessageRequest request) {
        return ApiResponse.success(partnerService.sendMessage(SecurityUtils.getCurrentUserId(), id, request));
    }

    @PostMapping("/conversations/{id}/read")
    public ApiResponse<PartnerMobileConversationVO> markConversationRead(@PathVariable("id") String id) {
        return ApiResponse.success(partnerService.markConversationRead(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/conversations/{id}/status")
    public ApiResponse<PartnerMobileConversationVO> updateConversationStatus(@PathVariable("id") String id,
                                                                             @Valid @RequestBody PartnerConversationStatusRequest request) {
        return ApiResponse.success(partnerService.updateConversationStatus(SecurityUtils.getCurrentUserId(), id, request));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<PartnerMobileProfileVO> userProfile(@PathVariable("id") Long id) {
        return ApiResponse.success(partnerService.userProfile(id));
    }
}
