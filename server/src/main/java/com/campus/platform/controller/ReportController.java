package com.campus.platform.controller;

import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.ReportCreateRequest;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.ReportService;
import com.campus.platform.vo.ReportVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ApiResponse<ReportVO> create(@Valid @RequestBody ReportCreateRequest request) {
        return ApiResponse.success(reportService.create(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping
    public ApiResponse<List<ReportVO>> myReports() {
        return ApiResponse.success(reportService.myReports(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(reportService.detail(SecurityUtils.getCurrentUserId(), id));
    }
}

