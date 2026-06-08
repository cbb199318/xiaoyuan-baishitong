package com.campus.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.platform.common.ApiResponse;
import com.campus.platform.dto.BeautyGoodCreateRequest;
import com.campus.platform.security.SecurityUtils;
import com.campus.platform.service.BeautyGoodService;
import com.campus.platform.vo.BeautyGoodVO;
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
@RequestMapping("/beauty")
@RequiredArgsConstructor
public class BeautyGoodController {

    private final BeautyGoodService beautyGoodService;

    @GetMapping("/goods")
    public ApiResponse<Page<BeautyGoodVO>> goods(@RequestParam(defaultValue = "") String keyword,
                                                 @RequestParam(defaultValue = "") String category,
                                                 @RequestParam(defaultValue = "") String priceRange,
                                                 @RequestParam(defaultValue = "") String skinTag,
                                                 @RequestParam(defaultValue = "1") int current,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(
            beautyGoodService.pageApproved(keyword, category, priceRange, skinTag, current, size, SecurityUtils.getCurrentUserIdOrNull())
        );
    }

    @GetMapping("/goods/{id}")
    public ApiResponse<BeautyGoodVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(beautyGoodService.detail(id, SecurityUtils.getCurrentUserIdOrNull()));
    }

    @PostMapping("/goods")
    public ApiResponse<BeautyGoodVO> create(@Valid @RequestBody BeautyGoodCreateRequest request) {
        return ApiResponse.success(beautyGoodService.create(SecurityUtils.getCurrentUserId(), request));
    }

    @GetMapping("/goods/mine")
    public ApiResponse<List<BeautyGoodVO>> myGoods() {
        return ApiResponse.success(beautyGoodService.myGoods(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/favorites")
    public ApiResponse<List<BeautyGoodVO>> favorites() {
        return ApiResponse.success(beautyGoodService.favorites(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/goods/{id}/favorite")
    public ApiResponse<BeautyGoodVO> favorite(@PathVariable("id") Long id) {
        return ApiResponse.success(beautyGoodService.favorite(SecurityUtils.getCurrentUserId(), id));
    }

    @PostMapping("/goods/{id}/unfavorite")
    public ApiResponse<BeautyGoodVO> unfavorite(@PathVariable("id") Long id) {
        return ApiResponse.success(beautyGoodService.unfavorite(SecurityUtils.getCurrentUserId(), id));
    }
}
