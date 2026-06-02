package com.campus.platform.controller;

import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.VerificationSubmitRequest;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.VerificationService;
import com.campus.platform.vo.VerificationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/submit")
    public ApiResponse<VerificationVO> submit(@Valid @RequestBody VerificationSubmitRequest request) {
        return ApiResponse.success(verificationService.submit(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/current")
    public ApiResponse<VerificationVO> current() {
        return ApiResponse.success(verificationService.getCurrent(SecurityUtils.getCurrentUserId()));
    }
}

